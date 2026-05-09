#!/bin/bash
# Run this ONCE from inside your dammroute folder
# Replace YOUR_GITHUB_USERNAME if different from Borgesjesk

echo "=== DammRoute Git Setup ==="

cd dammroute

# Initialize
git init
git branch -M main

# Create .gitignore
cat > .gitignore << 'IGNORE'
target/
*.class
*.jar
*.war
.idea/
*.iml
.DS_Store
application-prod.properties
IGNORE

# First commit
git add .
git commit -m "feat: initial DammRoute — Spring Boot 3.3, security, route optimizer, CO2 calculator"

echo ""
echo "Now run:"
echo "  git remote add origin https://github.com/Borgesjesk/dammroute.git"
echo "  git push -u origin main"
echo ""
echo "Every 30 minutes run:"
echo "  git add . && git commit -m 'feat: [what you built]' && git push"
