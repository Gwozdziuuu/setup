# Install ansible roles
ansible-playbook playbook.yml -i inventory/hosts.ini -vv
# Run ansible playbook
ansible-playbook playbook.yml -i inventory/hosts.ini --ask-vault-pas

make init                       # pobierze kolekcję
make ping                       # sprawdź połączenie do hosta
make apply                      # wykona rolę (poprosi o hasło do Vaulta)
