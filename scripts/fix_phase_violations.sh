#!/bin/bash

# fix_phase_violations.sh
# Automatically fixes common phase violations (bytecode hooks in init() instead of preInit())

set -e

if [ $# -lt 1 ]; then
    echo "Usage: $0 <mod_source_directory>"
    echo "Example: $0 ../ModSources/betterdig"
    exit 1
fi

MOD_DIR="$1"
BACKUP_SUFFIX=".before_phase_fix"

echo "🔍 Scanning $MOD_DIR for phase violations..."

# Find Java files that have both bytecode manipulation AND init()
find "$MOD_DIR" -name "*.java" | while read -r java_file; do
    # Check if file has bytecode manipulation keywords
    if grep -q "HookManager\|Javassist\|CtClass\|CtMethod\|ClassPool" "$java_file"; then
        # Check if it's in init() method
        if grep -q "public void init()" "$java_file"; then
            echo ""
            echo "📋 Found potential violation: $(basename $java_file)"

            # Backup original
            cp "$java_file" "${java_file}${BACKUP_SUFFIX}"
            echo "   💾 Backup created: ${java_file}${BACKUP_SUFFIX}"

            # Check if preInit already exists
            if grep -q "public void preInit()" "$java_file"; then
                echo "   ⚠️  preInit() already exists - manual merge needed!"
                echo "   📝 Review: $java_file"
            else
                echo "   🔨 Attempting automated fix..."

                # Use Python for more sophisticated text manipulation
                python3 << 'PYTHON_SCRIPT' "$java_file"
import sys
import re

file_path = sys.argv[1]

with open(file_path, 'r') as f:
    content = f.read()

# Pattern: Find init() method body
init_pattern = r'(public\s+void\s+init\s*\([^)]*\)\s*\{)(.*?)(\n\s*\})'
match = re.search(init_pattern, content, re.DOTALL)

if not match:
    print("   ❌ Could not parse init() method structure")
    sys.exit(1)

init_body = match.group(2)

# Check if init body has Javassist/HookManager code
has_hooks = any(keyword in init_body for keyword in [
    'HookManager', 'Javassist', 'CtClass', 'CtMethod', 'ClassPool'
])

if not has_hooks:
    print("   ℹ️  No bytecode hooks found in init() body")
    sys.exit(0)

# Extract the bytecode portion (usually the whole body if it's just hooks)
# This is a simplified approach - may need manual review
hook_code = init_body.strip()

# Create preInit() method
preinit_method = f'''
    @Override
    public void preInit() {{
{hook_code}
    }}
'''

# Find position to insert preInit (before init)
init_start = match.start()

# Insert preInit before init
new_content = content[:init_start] + preinit_method + '\n\n    ' + content[init_start:]

# Clean up empty init() or add comment
new_content = re.sub(
    r'(public\s+void\s+init\s*\([^)]*\)\s*\{)\s*(\n\s*\})',
    r'\1\n        // Moved to preInit()\n    \2',
    new_content
)

with open(file_path, 'w') as f:
    f.write(new_content)

print("   ✅ Created preInit() and moved bytecode hooks")
print("   ⚠️  MANUAL REVIEW REQUIRED - verify logic is correct!")

PYTHON_SCRIPT

                if [ $? -eq 0 ]; then
                    echo "   ✅ Automated fix applied"
                    echo "   📝 Review changes: diff ${java_file}${BACKUP_SUFFIX} $java_file"
                fi
            fi
        fi
    fi
done

echo ""
echo "✅ Phase violation scan complete!"
echo ""
echo "📌 Next steps:"
echo "   1. Review all changes with: git diff"
echo "   2. Test compilation: ./gradlew build"
echo "   3. Remove backups if satisfied: find . -name '*${BACKUP_SUFFIX}' -delete"
echo "   4. Commit with message: 'fix: move bytecode hooks to preInit()'"
