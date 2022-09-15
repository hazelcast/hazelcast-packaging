# note: fedora has no wget by default (strange, yeah), so it's additional query beside those mentioned in repo
sudo yum -y install wget
wget https://repository.hazelcast.com/rpm/stable/hazelcast-rpm-stable.repo -O hazelcast-rpm-stable.repo
sudo mv hazelcast-rpm-stable.repo /etc/yum.repos.d/