#!/bin/bash

# AI Torque Plugin Build Script

echo "╔════════════════════════════════════════╗"
echo "║   AI TORQUE - OMEGA BOSS BUILD        ║"
echo "╚════════════════════════════════════════╝"
echo ""

# Check if Maven is installed
if ! command -v mvn &> /dev/null
then
    echo "❌ Maven is not installed. Please install Maven first."
    echo "   Visit: https://maven.apache.org/download.cgi"
    exit 1
fi

echo "✅ Maven found: $(mvn -version | head -n 1)"
echo ""

# Navigate to plugin directory
cd "$(dirname "$0")"

echo "🔨 Building AI Torque plugin..."
echo ""

# Clean and package
mvn clean package

# Check if build was successful
if [ $? -eq 0 ]; then
    echo ""
    echo "╔════════════════════════════════════════╗"
    echo "║   ✅ BUILD SUCCESSFUL!                ║"
    echo "╚════════════════════════════════════════╝"
    echo ""
    echo "📦 Plugin JAR location:"
    echo "   $(pwd)/target/AITorque-1.0.0-ALPHA.jar"
    echo ""
    echo "📋 Next steps:"
    echo "   1. Copy JAR to your server's plugins/ folder"
    echo "   2. Start/restart your server"
    echo "   3. Configure in plugins/AITorque/config.yml"
    echo "   4. Use /spawntorque to summon AI Torque"
    echo ""
else
    echo ""
    echo "╔════════════════════════════════════════╗"
    echo "║   ❌ BUILD FAILED                     ║"
    echo "╚════════════════════════════════════════╝"
    echo ""
    echo "Please check the error messages above."
    exit 1
fi
