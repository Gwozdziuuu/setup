#!/bin/bash

# Example script demonstrating local usage of Ansible playbooks
# This script can be used as a quick start for developers

set -e

echo "=========================================="
echo "Ansible CI/CD Playbooks - Local Usage"
echo "=========================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Function to display steps
print_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

# Function to display success
print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

# Function to display errors
print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if we're in the ansible directory
if [ ! -f "ansible.cfg" ]; then
    print_error "This script must be run from the ansible/ directory"
    exit 1
fi

# Check if Ansible is installed
if ! command -v ansible-playbook &> /dev/null; then
    print_error "Ansible is not installed!"
    echo "Install it with: pip install ansible==2.15.*"
    exit 1
fi

# Install required collections
print_step "Installing required Ansible collections..."
ansible-galaxy collection install -r requirements.yml
print_success "Collections installed"
echo ""

# Selection menu
echo "Choose an action to perform:"
echo "1) Build - Build the project"
echo "2) Test - Run tests"
echo "3) Quality Check - Run code quality checks"
echo "4) Package - Package the application"
echo "5) Docker Build - Build Docker image"
echo "6) Full Pipeline - Run complete CI pipeline"
echo "7) Custom - Provide custom parameters"
echo ""

read -p "Select option (1-7): " choice
echo ""

case $choice in
    1)
        print_step "Building project..."
        ansible-playbook playbooks/build.yml
        print_success "Project built"
        ;;
    2)
        print_step "Running tests..."
        ansible-playbook playbooks/test.yml
        print_success "Tests completed"
        ;;
    3)
        print_step "Running code quality checks..."
        ansible-playbook playbooks/quality-check.yml -e "run_checkstyle=true"
        print_success "Quality checks completed"
        ;;
    4)
        print_step "Packaging application..."
        ansible-playbook playbooks/package.yml
        print_success "Application packaged"
        ;;
    5)
        print_step "Building Docker image..."
        read -p "Enter image name (default: spring-app): " image_name
        image_name=${image_name:-spring-app}
        read -p "Enter image tag (default: latest): " image_tag
        image_tag=${image_tag:-latest}

        ansible-playbook playbooks/docker-build.yml \
            -e "image_name=${image_name}" \
            -e "image_tag=${image_tag}"
        print_success "Docker image built"
        ;;
    6)
        print_step "Running full CI pipeline..."
        read -p "Skip tests? (y/N): " skip_tests
        read -p "Run quality checks? (Y/n): " quality_checks
        read -p "Build Docker? (y/N): " build_docker

        # Convert answers to boolean
        skip_tests_bool=$([ "$skip_tests" = "y" ] && echo "true" || echo "false")
        quality_checks_bool=$([ "$quality_checks" = "n" ] && echo "false" || echo "true")
        build_docker_bool=$([ "$build_docker" = "y" ] && echo "true" || echo "false")

        ansible-playbook playbooks/ci-pipeline.yml \
            -e "skip_tests=${skip_tests_bool}" \
            -e "run_quality_checks=${quality_checks_bool}" \
            -e "build_docker=${build_docker_bool}"
        print_success "Pipeline completed"
        ;;
    7)
        read -p "Enter playbook path: " playbook_path
        read -p "Enter additional parameters (optional): " extra_vars

        if [ -z "$extra_vars" ]; then
            ansible-playbook "$playbook_path"
        else
            ansible-playbook "$playbook_path" -e "$extra_vars"
        fi
        print_success "Playbook executed"
        ;;
    *)
        print_error "Invalid option!"
        exit 1
        ;;
esac

echo ""
echo "=========================================="
echo "Done!"
echo "=========================================="