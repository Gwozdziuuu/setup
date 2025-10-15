# Przykłady użycia playbooków Ansible w różnych systemach CI/CD

Ten folder zawiera przykładowe konfiguracje dla różnych systemów CI/CD oraz skrypty pomocnicze.

## Pliki

### gitlab-ci.yml
Przykładowa konfiguracja dla GitLab CI/CD.

**Jak używać:**
```bash
# Skopiuj do głównego katalogu projektu
cp ansible/examples/gitlab-ci.yml .gitlab-ci.yml

# Edytuj według potrzeb i commituj
git add .gitlab-ci.yml
git commit -m "Add GitLab CI configuration"
git push
```

**Cechy:**
- Oddzielne stage dla każdego kroku
- Cache dla zależności Maven
- Artifacts dla testów i pakietów
- Wsparcie dla merge requests
- Opcjonalny deployment

### Jenkinsfile
Przykładowa konfiguracja dla Jenkins Pipeline.

**Jak używać:**
```bash
# Skopiuj do głównego katalogu projektu
cp ansible/examples/Jenkinsfile Jenkinsfile

# Edytuj według potrzeb i commituj
git add Jenkinsfile
git commit -m "Add Jenkins Pipeline configuration"
git push
```

**Cechy:**
- Declarative Pipeline syntax
- Docker agent
- Stage dla każdego kroku
- Post-build actions
- Manual approval dla produkcji

### local-usage.sh
Interaktywny skrypt do lokalnego uruchamiania playbooków.

**Jak używać:**
```bash
cd ansible
./examples/local-usage.sh
```

**Cechy:**
- Interaktywne menu
- Kolorowy output
- Automatyczna instalacja kolekcji
- Wsparcie dla custom parametrów

## Szybki start dla różnych platform

### GitHub Actions
GitHub Actions jest już skonfigurowany w `.github/workflows/`.

Uruchom workflow:
1. Push kod do branch `main` lub `develop`
2. Lub użyj workflow_dispatch w GitHub UI

### GitLab CI
```bash
# 1. Skopiuj konfigurację
cp ansible/examples/gitlab-ci.yml .gitlab-ci.yml

# 2. Commituj i pushuj
git add .gitlab-ci.yml
git commit -m "feat: add GitLab CI"
git push

# 3. Pipeline uruchomi się automatycznie
```

### Jenkins
```bash
# 1. Skopiuj konfigurację
cp ansible/examples/Jenkinsfile Jenkinsfile

# 2. Commituj i pushuj
git add Jenkinsfile
git commit -m "feat: add Jenkins Pipeline"
git push

# 3. Skonfiguruj Jenkins:
#    - Utwórz nowy Pipeline job
#    - Wybierz "Pipeline script from SCM"
#    - Podaj URL repozytorium
#    - Zapisz i uruchom
```

### CircleCI
```yaml
# .circleci/config.yml
version: 2.1

jobs:
  build:
    docker:
      - image: cimg/python:3.11
    steps:
      - checkout
      - run:
          name: Install Ansible
          command: pip install ansible==2.15.*
      - run:
          name: Install collections
          command: cd ansible && ansible-galaxy collection install -r requirements.yml
      - run:
          name: Build
          command: cd ansible && ansible-playbook playbooks/build.yml

  test:
    docker:
      - image: cimg/python:3.11
    steps:
      - checkout
      - run:
          name: Install Ansible
          command: pip install ansible==2.15.*
      - run:
          name: Install collections
          command: cd ansible && ansible-galaxy collection install -r requirements.yml
      - run:
          name: Test
          command: cd ansible && ansible-playbook playbooks/test.yml

workflows:
  version: 2
  build-test:
    jobs:
      - build
      - test:
          requires:
            - build
```

### Drone CI
```yaml
# .drone.yml
kind: pipeline
type: docker
name: default

steps:
  - name: setup
    image: python:3.11
    commands:
      - pip install ansible==2.15.*
      - cd ansible && ansible-galaxy collection install -r requirements.yml

  - name: build
    image: python:3.11
    commands:
      - cd ansible && ansible-playbook playbooks/build.yml

  - name: test
    image: python:3.11
    commands:
      - cd ansible && ansible-playbook playbooks/test.yml

  - name: package
    image: python:3.11
    commands:
      - cd ansible && ansible-playbook playbooks/package.yml
    when:
      branch:
        - main
```

## Lokalne testowanie

### Przed commitowaniem
```bash
# Uruchom pełny pipeline lokalnie
cd ansible
ansible-playbook playbooks/ci-pipeline.yml
```

### Quick check
```bash
# Tylko build i testy
cd ansible
ansible-playbook playbooks/build.yml
ansible-playbook playbooks/test.yml
```

### Debug mode
```bash
# Z verbose output
cd ansible
ansible-playbook playbooks/build.yml -vvv
```

## Dostosowywanie

### Zmiana wersji narzędzi
Edytuj zmienne w plikach CI:
- `JAVA_VERSION` - wersja Java
- `ANSIBLE_VERSION` - wersja Ansible
- `MAVEN_OPTS` - opcje Maven

### Dodanie własnych kroków
1. Utwórz nowy playbook w `playbooks/`
2. Dodaj krok w plikach CI
3. Dokumentuj w README

### Zmiana cache
Każdy system CI ma swój mechanizm cache:
- GitHub Actions: `actions/cache@v4`
- GitLab CI: `cache:` section
- Jenkins: workspace persistence

## Troubleshooting

### "Ansible not found"
```bash
pip install ansible==2.15.*
```

### "Collection not found"
```bash
cd ansible
ansible-galaxy collection install -r requirements.yml
```

### "Maven wrapper permission denied"
Playbooki automatycznie nadają uprawnienia dla `mvnw`.

### "Docker socket permission denied"
W Jenkins/GitLab CI upewnij się, że runner ma dostęp do Docker socket.

## Dodatkowe zasoby

- [Ansible Documentation](https://docs.ansible.com/)
- [GitHub Actions Documentation](https://docs.github.com/actions)
- [GitLab CI Documentation](https://docs.gitlab.com/ee/ci/)
- [Jenkins Pipeline Documentation](https://www.jenkins.io/doc/book/pipeline/)