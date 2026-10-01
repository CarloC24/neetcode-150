#!/usr/bin/env bash
#
# Run every solution in the repo, in both languages, and report what failed.
#
# Usage:
#   ./example.sh                 # every problem, Python and Java
#   ./example.sh linked          # only problem folders whose name contains "linked"
#   ./example.sh -p              # Python only
#   ./example.sh -j two-sum      # Java only, one problem
#   ./example.sh -v              # also print each file's own output
#
# A file counts as failing if it exits non-zero (a crash, an exception, a
# compile error) or prints any line starting with FAIL. Exits 1 if anything
# failed, so it can gate a commit.
#
# Plain bash 3.2, so it runs on the macOS system shell without Homebrew.

set -u

cd "$(dirname "$0")" || exit 1

run_python=1
run_java=1
verbose=0
filter=""

while [ $# -gt 0 ]; do
    case "$1" in
        -p | --python) run_java=0 ;;
        -j | --java) run_python=0 ;;
        -v | --verbose) verbose=1 ;;
        -h | --help)
            sed -n '3,15p' "$0" | sed 's/^# \{0,1\}//'
            exit 0
            ;;
        -*)
            echo "unknown option: $1 (try --help)" >&2
            exit 2
            ;;
        *) filter="$1" ;;
    esac
    shift
done

if [ -t 1 ]; then
    green=$'\033[32m' red=$'\033[31m' dim=$'\033[2m' bold=$'\033[1m' reset=$'\033[0m'
else
    green="" red="" dim="" bold="" reset=""
fi

passed=0
failed=0
failures=""

# run_one LABEL COMMAND...
run_one() {
    local label="$1"
    shift

    local output status
    output="$("$@" 2>&1)"
    status=$?

    if [ $status -eq 0 ] && ! printf '%s\n' "$output" | grep -q '^FAIL'; then
        printf '  %sPASS%s  %s\n' "$green" "$reset" "$label"
        passed=$((passed + 1))
        if [ $verbose -eq 1 ]; then
            printf '%s\n' "$output" | sed "s/^/        ${dim}/;s/\$/${reset}/"
        fi
    else
        printf '  %sFAIL%s  %s  %s(exit %d)%s\n' "$red" "$reset" "$label" "$dim" "$status" "$reset"
        printf '%s\n' "$output" | sed 's/^/        /'
        failed=$((failed + 1))
        failures="${failures}  ${dir}  ${label}"$'\n'
    fi
}

if [ $run_python -eq 1 ] && ! command -v python3 > /dev/null; then
    echo "python3 not found; skipping Python (use -j to silence this)" >&2
    run_python=0
fi
if [ $run_java -eq 1 ] && ! command -v java > /dev/null; then
    echo "java not found; skipping Java (use -p to silence this)" >&2
    run_java=0
fi

problems=0
for dir in */; do
    dir="${dir%/}"
    case "$dir" in *"$filter"*) ;; *) continue ;; esac
    [ -d "$dir/python" ] || [ -d "$dir/java" ] || continue

    problems=$((problems + 1))
    printf '%s%s%s\n' "$bold" "$dir" "$reset"

    if [ $run_python -eq 1 ]; then
        for file in "$dir"/python/*.py; do
            [ -e "$file" ] && run_one "python  $(basename "$file")" python3 "$file"
        done
    fi
    if [ $run_java -eq 1 ]; then
        for file in "$dir"/java/*.java; do
            [ -e "$file" ] && run_one "java    $(basename "$file")" java "$file"
        done
    fi
done

if [ $problems -eq 0 ]; then
    echo "no problem folders match \"$filter\"" >&2
    exit 2
fi

echo
if [ $failed -eq 0 ]; then
    printf '%s%d passed%s across %d problems\n' "$green" "$passed" "$reset" "$problems"
else
    printf '%s%d failed%s, %d passed across %d problems:\n' "$red" "$failed" "$reset" "$passed" "$problems"
    printf '%s' "$failures"
    exit 1
fi
