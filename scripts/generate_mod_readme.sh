#!/bin/bash

# generate_mod_readme.sh
# Generates attribution README.md for a modernized community mod

set -e

if [ $# -lt 5 ]; then
    echo "Usage: $0 <mod_name> <original_author> <original_repo> <license> <output_dir>"
    echo "Example: $0 betterdig bdew https://github.com/bdew-wurm/betterdig MIT ./community/bdew/betterdig"
    exit 1
fi

MOD_NAME="$1"
ORIGINAL_AUTHOR="$2"
ORIGINAL_REPO="$3"
LICENSE="$4"
OUTPUT_DIR="$5"

mkdir -p "$OUTPUT_DIR"

# Generate README.md
cat > "$OUTPUT_DIR/README.md" << EOF
# $MOD_NAME (Modernized for WurmModLoader)

**Original Author:** $ORIGINAL_AUTHOR
**Original Source:** $ORIGINAL_REPO
**Original License:** $LICENSE
**Modernized By:** garward
**WurmModLoader Version:** 1.0.0+

## 🎯 Modernization Changes

This version has been updated to work with the modern WurmModLoader framework:

- ✅ Migrated from Ago's modloader to WurmModLoader API
- ✅ Moved bytecode hooks from \`init()\` to \`preInit()\` (proper phase)
- ✅ Replaced event hooks with \`@SubscribeEvent\` pattern
- ✅ [If applicable] Migrated manual database code to Capability system
- ✅ [If applicable] Updated to use Registry system with ResourceLocation
- ✅ Compiled with Java 8 compatibility for Wurm Unlimited

## 📜 Original Description

[TODO: Copy from original README]

## 🔧 Installation

1. Download \`$MOD_NAME.jar\` from releases
2. Copy to your Wurm Unlimited server's \`mods/$MOD_NAME/\` directory
3. Copy \`$MOD_NAME.properties\` to \`mods/\` directory
4. Restart server

## ⚙️ Configuration

[TODO: Document config options]

## 🙏 Credits

All credit for the original design and implementation goes to **$ORIGINAL_AUTHOR**.

This version simply adapts it to work with the modern WurmModLoader framework.

## 📝 License

This mod maintains the original $LICENSE license.

See [LICENSE](LICENSE) for details.

## 🐛 Issues

For issues specific to the WurmModLoader version, please report them here.

For issues with the original mod logic, please report to the [original repository]($ORIGINAL_REPO).

---

**This is a community modernization - not affiliated with the original author unless explicitly stated.**

If you're the original author and would like to take over maintenance, please open an issue!
EOF

# Generate CHANGELOG.md
cat > "$OUTPUT_DIR/CHANGELOG.md" << EOF
# Changelog - $MOD_NAME (WurmModLoader)

## [Unreleased] - Modernization

### Changed
- Migrated to WurmModLoader API
- Moved bytecode instrumentation to preInit() phase
- [TODO: List specific changes]

### Added
- WurmModLoader event system integration
- [TODO: List new features if any]

### Removed
- [TODO: List removed features if any]

---

## Original Changelog

See [$ORIGINAL_REPO/releases]($ORIGINAL_REPO/releases) for original version history.
EOF

# Copy LICENSE if it exists in original repo
# This would need to be done manually or via GitHub API

echo "✅ Generated documentation for $MOD_NAME"
echo "   📄 $OUTPUT_DIR/README.md"
echo "   📄 $OUTPUT_DIR/CHANGELOG.md"
echo ""
echo "📌 Next steps:"
echo "   1. Fill in [TODO] sections in README.md"
echo "   2. Update CHANGELOG.md with specific changes"
echo "   3. Copy LICENSE file from original repository"
echo "   4. Add mod source code to $OUTPUT_DIR/src/"
