#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

if [ -f .env ]; then
  set -a
  . ./.env
  set +a
fi

PORT=${WEB_PORT:-20452}
BASE_URL="http://localhost:${PORT}"

PASS=0
FAIL=0
TOTAL=0

RANGER_ID=""
QUARANTINE_ID=""
DISPOSAL_ID=""
PK_TRAP=""
PK_TRAP_2=""
PK_RECORD_NORMAL=""
PK_RECORD_SUSPECT=""
PK_RECORD_HR1=""
PK_RECORD_HR2=""
PK_RECORD_HR3=""
PK_REVIEW=""
PK_DISPOSAL=""

green() { echo -e "\033[32m✅ $1\033[0m"; }
red() { echo -e "\033[31m❌ $1\033[0m"; }
yellow() { echo -e "\033[33m📝 $1\033[0m"; }
blue() { echo -e "\033[34m🔍 $1\033[0m"; }

assert() {
    local desc="$1"
    local cond="$2"
    TOTAL=$((TOTAL + 1))
    if eval "$cond"; then
        PASS=$((PASS + 1))
        green "$desc"
    else
        FAIL=$((FAIL + 1))
        red "$desc"
    fi
}

assert_contains() {
    local desc="$1"
    local text="$2"
    local keyword="$3"
    if echo "$text" | grep -q "$keyword"; then
        assert "$desc" "true"
    else
        assert "$desc (期望包含: $keyword, 实际: $text)" "false"
    fi
}

http_post() {
    local url="$1"
    local data="$2"
    local user_header="${3:-}"
    local auth_headers=""
    if [ -n "$user_header" ]; then
        auth_headers="-H userId:${user_header} -H pkOrg:ORG001 -H pkGroup:GRP001"
    fi
    curl -s -X POST "$url" \
        -H "Content-Type: application/json" \
        $auth_headers \
        -d "$data" 2>/dev/null
}

http_get() {
    local url="$1"
    local user_header="${2:-}"
    local auth_headers=""
    if [ -n "$user_header" ]; then
        auth_headers="-H userId:${user_header} -H pkOrg:ORG001 -H pkGroup:GRP001"
    fi
    curl -s "$url" $auth_headers 2>/dev/null
}

wait_for_server() {
    yellow "等待服务启动..."
    local max_wait=30
    local waited=0
    while [ $waited -lt $max_wait ]; do
        if curl -s "${BASE_URL}/" >/dev/null 2>&1; then
            green "服务已就绪 (${BASE_URL})"
            return 0
        fi
        sleep 1
        waited=$((waited + 1))
        echo -n "."
    done
    echo ""
    red "服务启动超时"
    return 1
}

extract_json_field() {
    local json="$1"
    local field="$2"
    python3 -c "$(cat <<'PYEOF'
import sys, json
try:
    data = json.loads(sys.argv[1])
    key = sys.argv[2]
    def find(obj, k):
        if isinstance(obj, dict):
            if k in obj:
                return str(obj[k])
            for v in obj.values():
                r = find(v, k)
                if r is not None:
                    return r
        elif isinstance(obj, list):
            for item in obj:
                r = find(item, k)
                if r is not None:
                    return r
        return None
    result = find(data, key)
    print(result if result is not None and result != "None" else "")
except Exception as e:
    print("")
PYEOF
)" "$json" "$field"
}

echo ""
echo "============================================"
echo "  林业有害生物诱捕监测系统 - 回归验证测试"
echo "============================================"
echo ""

wait_for_server || exit 1

echo ""
echo "============================================"
echo "  一、用户登录与角色验证"
echo "============================================"

yellow "1.1 护林员登录"
RESP=$(http_post "${BASE_URL}/api/forest/user?action=login" '{"userCode":"ranger001","password":"123456"}')
assert_contains "护林员登录成功" "$RESP" "\"success\":true"
RANGER_ID=$(extract_json_field "$RESP" "pk_forest_user")
assert "返回用户ID" "[ -n \"$RANGER_ID\" ]"

yellow "1.2 检疫员登录"
RESP=$(http_post "${BASE_URL}/api/forest/user?action=login" '{"userCode":"quarantine001","password":"123456"}')
assert_contains "检疫员登录成功" "$RESP" "\"success\":true"
QUARANTINE_ID=$(extract_json_field "$RESP" "pk_forest_user")
assert "返回用户ID" "[ -n \"$QUARANTINE_ID\" ]"

yellow "1.3 处置队登录"
RESP=$(http_post "${BASE_URL}/api/forest/user?action=login" '{"userCode":"disposal001","password":"123456"}')
assert_contains "处置队登录成功" "$RESP" "\"success\":true"
DISPOSAL_ID=$(extract_json_field "$RESP" "pk_forest_user")
assert "返回用户ID" "[ -n \"$DISPOSAL_ID\" ]"

yellow "1.4 错误密码登录失败"
RESP=$(http_post "${BASE_URL}/api/forest/user?action=login" '{"userCode":"ranger001","password":"wrong"}')
assert_contains "错误密码登录失败" "$RESP" "\"success\":false"

echo ""
echo "============================================"
echo "  二、护林员：登记诱捕器位置和虫情数量"
echo "============================================"

yellow "2.1 查询诱捕器点位列表"
RESP=$(http_get "${BASE_URL}/api/forest/trap?action=list" "$RANGER_ID")
assert_contains "查询诱捕器点位成功" "$RESP" "\"success\":true"
PK_TRAP=$(echo "$RESP" | grep -o '"pk_forest_trap":"[^"]*"' | head -1 | sed 's/.*:"\([^"]*\)"/\1/')
assert "存在诱捕器点位数据" "[ -n \"$PK_TRAP\" ]"

yellow "2.2 新增诱捕器点位 (地图坐标落库)"
TODAY=$(date +%Y-%m-%d)
RESP=$(http_post "${BASE_URL}/api/forest/trap?action=save" '{
    "trap_code":"TRAP-TEST-001",
    "trap_name":"测试诱捕器-回归测试",
    "longitude":116.500,
    "latitude":39.950,
    "location_desc":"回归测试区域A",
    "forest_type":"松林",
    "trap_type":"性诱剂",
    "install_date":"'"$TODAY"'",
    "pk_ranger":"'"$RANGER_ID"'"
}' "$RANGER_ID")
assert_contains "新增诱捕器成功" "$RESP" "\"success\":true"
PK_TRAP_2=$(extract_json_field "$RESP" "pk_forest_trap")
assert "生成诱捕器主键" "[ -n \"$PK_TRAP_2\" ]"

yellow "2.3 查询新增点位确认地图坐标落库"
RESP=$(http_get "${BASE_URL}/api/forest/trap?action=query&pk=${PK_TRAP_2}" "$RANGER_ID")
assert_contains "查询诱捕器成功" "$RESP" "\"success\":true"
LONGITUDE=$(extract_json_field "$RESP" "longitude")
LATITUDE=$(extract_json_field "$RESP" "latitude")
assert "经度落库正确 ($LONGITUDE)" "[ \"$LONGITUDE\" = \"116.5\" ]"
assert "纬度落库正确 ($LATITUDE)" "[ \"$LATITUDE\" = \"39.95\" ]"

yellow "2.4 护林员登记正常虫情"
RESP=$(http_post "${BASE_URL}/api/forest/record?action=save" '{
    "pk_forest_trap":"'"$PK_TRAP"'",
    "record_date":"'"$TODAY"'",
    "insect_type":"松墨天牛",
    "insect_count":5,
    "is_suspect_quarantine":0
}' "$RANGER_ID")
assert_contains "正常虫情登记成功" "$RESP" "\"success\":true"
PK_RECORD_NORMAL=$(extract_json_field "$RESP" "pk_trap_record")
assert "生成诱捕记录ID" "[ -n \"$PK_RECORD_NORMAL\" ]"

yellow "2.5 护林员登记疑似检疫对象虫情"
RESP=$(http_post "${BASE_URL}/api/forest/record?action=save" '{
    "pk_forest_trap":"'"$PK_TRAP"'",
    "record_date":"'"$TODAY"'",
    "insect_type":"美国白蛾",
    "insect_count":20,
    "is_suspect_quarantine":1,
    "suspect_remark":"疑似检测到美国白蛾幼虫"
}' "$RANGER_ID")
assert_contains "疑似检疫对象登记成功" "$RESP" "\"success\":true"
PK_RECORD_SUSPECT=$(extract_json_field "$RESP" "pk_trap_record")
assert "生成疑似检疫记录ID" "[ -n \"$PK_RECORD_SUSPECT\" ]"

yellow "2.6 验证诱捕记录落库 (状态=待复核)"
RESP=$(http_get "${BASE_URL}/api/forest/record?action=query&pk=${PK_RECORD_SUSPECT}" "$RANGER_ID")
assert_contains "查询诱捕记录成功" "$RESP" "\"success\":true"
STATUS=$(extract_json_field "$RESP" "record_status")
SUSPECT=$(extract_json_field "$RESP" "is_suspect_quarantine")
assert "记录状态=待复核(0)" "[ \"$STATUS\" = \"0\" ]"
assert "疑似检疫标记=是(1)" "[ \"$SUSPECT\" = \"1\" ]"

echo ""
echo "============================================"
echo "  三、检疫员：复核风险等级 (规则1验证)"
echo "============================================"

yellow "3.1 规则1验证 - 疑似检疫对象未复核前尝试创建处置单 - 应该失败"
RESP=$(http_post "${BASE_URL}/api/forest/disposal?action=create" '{
    "pk_trap_record":"'"$PK_RECORD_SUSPECT"'",
    "disposal_date":"'"$TODAY"'",
    "disposal_type":"疫木清理",
    "disposal_method":"采伐烧毁",
    "tree_count":10
}' "$DISPOSAL_ID")
assert_contains "未复核的疑似检疫对象创建处置单被拒绝" "$RESP" "\"success\":false"

yellow "3.2 检疫员复核正常虫情 (低风险)"
RESP=$(http_post "${BASE_URL}/api/forest/review?action=save" '{
    "pk_trap_record":"'"$PK_RECORD_NORMAL"'",
    "review_date":"'"$TODAY"'",
    "risk_level":1,
    "is_quarantine":0,
    "is_allow_disposal":0,
    "review_remark":"正常监测，低风险"
}' "$QUARANTINE_ID")
assert_contains "正常记录复核成功" "$RESP" "\"success\":true"

yellow "3.3 验证复核记录落库"
RESP=$(http_get "${BASE_URL}/api/forest/review?action=list&pk_trap_record=${PK_RECORD_NORMAL}" "$QUARANTINE_ID")
assert_contains "复核记录落库成功" "$RESP" "\"success\":true"
RISK=$(extract_json_field "$RESP" "risk_level")
assert "复核风险等级=低风险(1)" "[ \"$RISK\" = \"1\" ]"

yellow "3.4 验证诱捕记录状态更新为已复核"
RESP=$(http_get "${BASE_URL}/api/forest/record?action=query&pk=${PK_RECORD_NORMAL}" "$RANGER_ID")
STATUS=$(extract_json_field "$RESP" "record_status")
RISK_LEVEL=$(extract_json_field "$RESP" "risk_level")
assert "诱捕记录状态=已复核(1)" "[ \"$STATUS\" = \"1\" ]"
assert "诱捕记录风险等级更新=低风险(1)" "[ \"$RISK_LEVEL\" = \"1\" ]"

yellow "3.5 检疫员复核疑似检疫对象 - 不允许清理"
RESP=$(http_post "${BASE_URL}/api/forest/review?action=save" '{
    "pk_trap_record":"'"$PK_RECORD_SUSPECT"'",
    "review_date":"'"$TODAY"'",
    "risk_level":3,
    "is_quarantine":1,
    "is_allow_disposal":0,
    "review_remark":"确认为检疫对象，暂不允许清理，需进一步评估"
}' "$QUARANTINE_ID")
assert_contains "疑似记录复核成功" "$RESP" "\"success\":true"
PK_REVIEW=$(extract_json_field "$RESP" "pk_forest_review")
assert "生成复核记录ID" "[ -n \"$PK_REVIEW\" ]"

yellow "3.6 规则1再验证 - 复核不允许清理的检疫对象尝试创建处置单 - 应该失败"
RESP=$(http_post "${BASE_URL}/api/forest/disposal?action=create" '{
    "pk_trap_record":"'"$PK_RECORD_SUSPECT"'",
    "disposal_date":"'"$TODAY"'",
    "disposal_type":"疫木清理",
    "disposal_method":"采伐烧毁",
    "tree_count":10
}' "$DISPOSAL_ID")
assert_contains "复核不允许清理时创建处置单被拒绝" "$RESP" "\"success\":false"

echo ""
echo "============================================"
echo "  四、规则3验证 - 连续三次高风险自动进入重点巡查"
echo "============================================"

yellow "4.1 登记第1次高风险虫情"
RESP=$(http_post "${BASE_URL}/api/forest/record?action=save" '{
    "pk_forest_trap":"'"$PK_TRAP_2"'",
    "record_date":"'"$TODAY"'",
    "insect_type":"松墨天牛",
    "insect_count":100,
    "is_suspect_quarantine":0
}' "$RANGER_ID")
PK_RECORD_HR1=$(extract_json_field "$RESP" "pk_trap_record")

RESP=$(http_post "${BASE_URL}/api/forest/review?action=save" '{
    "pk_trap_record":"'"$PK_RECORD_HR1"'",
    "review_date":"'"$TODAY"'",
    "risk_level":3,
    "is_quarantine":0,
    "review_remark":"高风险，虫口密度大"
}' "$QUARANTINE_ID")
assert_contains "第1次高风险复核成功" "$RESP" "\"success\":true"

yellow "4.2 登记第2次高风险虫情"
RESP=$(http_post "${BASE_URL}/api/forest/record?action=save" '{
    "pk_forest_trap":"'"$PK_TRAP_2"'",
    "record_date":"'"$TODAY"'",
    "insect_type":"松墨天牛",
    "insect_count":150,
    "is_suspect_quarantine":0
}' "$RANGER_ID")
PK_RECORD_HR2=$(extract_json_field "$RESP" "pk_trap_record")

RESP=$(http_post "${BASE_URL}/api/forest/review?action=save" '{
    "pk_trap_record":"'"$PK_RECORD_HR2"'",
    "review_date":"'"$TODAY"'",
    "risk_level":3,
    "is_quarantine":0,
    "review_remark":"高风险，持续监测"
}' "$QUARANTINE_ID")
assert_contains "第2次高风险复核成功" "$RESP" "\"success\":true"

yellow "4.3 检查当前重点巡查状态 (2次高风险，应该还没自动进入)"
RESP=$(http_get "${BASE_URL}/api/forest/trap?action=query&pk=${PK_TRAP_2}" "$RANGER_ID")
KEY_PATROL=$(extract_json_field "$RESP" "is_key_patrol")
assert "2次高风险时重点巡查标记=否(0)" "[ \"$KEY_PATROL\" = \"0\" ]"

yellow "4.4 登记第3次高风险虫情 (触法自动重点巡查)"
RESP=$(http_post "${BASE_URL}/api/forest/record?action=save" '{
    "pk_forest_trap":"'"$PK_TRAP_2"'",
    "record_date":"'"$TODAY"'",
    "insect_type":"松墨天牛",
    "insect_count":200,
    "is_suspect_quarantine":0
}' "$RANGER_ID")
PK_RECORD_HR3=$(extract_json_field "$RESP" "pk_trap_record")

RESP=$(http_post "${BASE_URL}/api/forest/review?action=save" '{
    "pk_trap_record":"'"$PK_RECORD_HR3"'",
    "review_date":"'"$TODAY"'",
    "risk_level":3,
    "is_quarantine":0,
    "review_remark":"高风险，已连续3次"
}' "$QUARANTINE_ID")
assert_contains "第3次高风险复核成功" "$RESP" "\"success\":true"

yellow "4.5 规则3验证 - 检查是否自动进入重点巡查"
sleep 1
RESP=$(http_get "${BASE_URL}/api/forest/trap?action=query&pk=${PK_TRAP_2}" "$RANGER_ID")
KEY_PATROL=$(extract_json_field "$RESP" "is_key_patrol")
KEY_REASON=$(extract_json_field "$RESP" "key_patrol_reason")
assert "连续3次高风险后重点巡查标记=是(1)" "[ \"$KEY_PATROL\" = \"1\" ]"
assert "自动生成重点巡查原因说明" "[ -n \"$KEY_REASON\" ]"
blue "重点巡查原因: $KEY_REASON"

echo ""
echo "============================================"
echo "  五、处置队：疫木清理 (规则2验证)"
echo "============================================"

yellow "5.1 处置队创建处置单 (正常已复核记录)"
RESP=$(http_post "${BASE_URL}/api/forest/disposal?action=create" '{
    "pk_trap_record":"'"$PK_RECORD_NORMAL"'",
    "disposal_date":"'"$TODAY"'",
    "disposal_type":"疫木清理",
    "disposal_method":"采伐烧毁",
    "tree_count":5,
    "disposal_area":1.5,
    "disposal_remark":"清理完成待拍照"
}' "$DISPOSAL_ID")
assert_contains "处置单创建成功" "$RESP" "\"success\":true"
PK_DISPOSAL=$(extract_json_field "$RESP" "pk_forest_disposal")
assert "生成处置单ID" "[ -n \"$PK_DISPOSAL\" ]"

yellow "5.2 验证处置单落库"
RESP=$(http_get "${BASE_URL}/api/forest/disposal?action=query&pk=${PK_DISPOSAL}" "$DISPOSAL_ID")
assert_contains "查询处置单成功" "$RESP" "\"success\":true"
STATUS=$(extract_json_field "$RESP" "disposal_status")
HAS_PHOTO=$(extract_json_field "$RESP" "has_photo")
assert "处置单状态=待处置(0)" "[ \"$STATUS\" = \"0\" ]"
assert "处置单拍照标记=否(0)" "[ \"$HAS_PHOTO\" = \"0\" ]"

yellow "5.3 开始处置"
RESP=$(http_post "${BASE_URL}/api/forest/disposal?action=start" "{\"pk\":\"${PK_DISPOSAL}\"}" "$DISPOSAL_ID")
assert_contains "开始处置成功" "$RESP" "\"success\":true"

yellow "5.4 规则2验证 - 未上传照片关闭处置单 - 应该失败"
RESP=$(http_post "${BASE_URL}/api/forest/disposal?action=close" "{\"pk\":\"${PK_DISPOSAL}\"}" "$DISPOSAL_ID")
assert_contains "未拍照关闭处置单被拒绝" "$RESP" "\"success\":false"
MSG=$(extract_json_field "$RESP" "data")
assert "错误提示包含'未拍照'" "echo \"$MSG\" | grep -q '未拍照'"

yellow "5.5 上传清理照片"
RESP=$(http_post "${BASE_URL}/api/forest/disposal?action=uploadPhoto" '{
    "pk_forest_disposal":"'"$PK_DISPOSAL"'",
    "photo_type":"清理后照片",
    "photo_url":"https://example.com/photos/cleanup-'${PK_DISPOSAL}'.jpg",
    "photo_remark":"疫木清理完成现场照片"
}' "$DISPOSAL_ID")
assert_contains "照片上传成功" "$RESP" "\"success\":true"

yellow "5.6 验证照片标记更新"
RESP=$(http_get "${BASE_URL}/api/forest/disposal?action=query&pk=${PK_DISPOSAL}" "$DISPOSAL_ID")
HAS_PHOTO=$(extract_json_field "$RESP" "has_photo")
assert "拍照标记已更新=是(1)" "[ \"$HAS_PHOTO\" = \"1\" ]"

yellow "5.7 规则2再验证 - 已上传照片关闭处置单 - 应该成功"
RESP=$(http_post "${BASE_URL}/api/forest/disposal?action=close" "{\"pk\":\"${PK_DISPOSAL}\"}" "$DISPOSAL_ID")
assert_contains "已拍照关闭处置单成功" "$RESP" "\"success\":true"

yellow "5.8 验证处置单和诱捕记录状态更新"
RESP=$(http_get "${BASE_URL}/api/forest/disposal?action=query&pk=${PK_DISPOSAL}" "$DISPOSAL_ID")
D_STATUS=$(extract_json_field "$RESP" "disposal_status")
assert "处置单状态=已完成(2)" "[ \"$D_STATUS\" = \"2\" ]"

RESP=$(http_get "${BASE_URL}/api/forest/record?action=query&pk=${PK_RECORD_NORMAL}" "$RANGER_ID")
R_STATUS=$(extract_json_field "$RESP" "record_status")
assert "诱捕记录状态=已处置(2)" "[ \"$R_STATUS\" = \"2\" ]"

echo ""
echo "============================================"
echo "  六、数据落库完整性验证"
echo "============================================"

yellow "6.1 验证诱捕器点位数据 (地图坐标)"
RESP=$(http_get "${BASE_URL}/api/forest/trap?action=list" "$RANGER_ID")
TRAP_COUNT=$(echo "$RESP" | grep -o '"pk_forest_trap"' | wc -l)
assert "诱捕器点位数量 >= 3 (原有3个+新增1个=至少4个)" "[ $TRAP_COUNT -ge 3 ]"
blue "点位数量: $TRAP_COUNT"

yellow "6.2 验证诱捕记录数据"
RESP=$(http_get "${BASE_URL}/api/forest/record?action=list" "$RANGER_ID")
RECORD_COUNT=$(echo "$RESP" | grep -o '"pk_trap_record"' | wc -l)
assert "诱捕记录数量 >= 5" "[ $RECORD_COUNT -ge 5 ]"
blue "诱捕记录数量: $RECORD_COUNT"

yellow "6.3 验证复核记录数据"
RESP=$(http_get "${BASE_URL}/api/forest/review?action=list" "$QUARANTINE_ID")
REVIEW_COUNT=$(echo "$RESP" | grep -o '"pk_forest_review"' | wc -l)
assert "复核记录数量 >= 5" "[ $REVIEW_COUNT -ge 5 ]"
blue "复核记录数量: $REVIEW_COUNT"

yellow "6.4 验证处置记录数据"
RESP=$(http_get "${BASE_URL}/api/forest/disposal?action=list" "$DISPOSAL_ID")
DISPOSAL_COUNT=$(echo "$RESP" | grep -o '"pk_forest_disposal"' | wc -l)
assert "处置记录数量 >= 1" "[ $DISPOSAL_COUNT -ge 1 ]"
blue "处置记录数量: $DISPOSAL_COUNT"

yellow "6.5 验证重点巡查点位列表"
RESP=$(http_get "${BASE_URL}/api/forest/trap?action=list&is_key_patrol=1" "$QUARANTINE_ID")
KEY_COUNT=$(echo "$RESP" | grep -o '"pk_forest_trap"' | wc -l)
assert "重点巡查点位数量 >= 1" "[ $KEY_COUNT -ge 1 ]"
blue "重点巡查点位数量: $KEY_COUNT"

echo ""
echo "============================================"
echo "  回归验证测试结果汇总"
echo "============================================"
echo ""
echo "  总测试项: $TOTAL"
echo "  ✅ 通过: $PASS"
echo "  ❌ 失败: $FAIL"
echo ""
if [ $FAIL -eq 0 ]; then
    green "所有测试通过！系统功能验证完成"
else
    red "存在失败测试项，请检查相关功能"
    exit 1
fi

echo ""
echo "三角色使用入口:"
echo "  护林员: ${BASE_URL}/ranger.html"
echo "  检疫员: ${BASE_URL}/quarantine.html"
echo "  处置队: ${BASE_URL}/disposal.html"
echo ""
