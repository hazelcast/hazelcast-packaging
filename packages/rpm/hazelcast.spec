%define hzversion 4.1-BETA-1

Name:		hazelcast
Version:    4.2020.10
Release:	1
Summary:	A tool that allows users to install & run Hazelcast and Management Center on the local environment

License:	ASL 2.0
URL:		https://hazelcast.org/

Source0:    hazelcast-%{hzversion}.tar.gz

Requires:	java-1.8.0-devel

BuildArch:  noarch

%description
A tool that allows users to install & run Hazelcast and Management Center on the local environment

%prep
%setup -c %{name}-%{hzversion}

%build
true

%pre
echo "Installing Hazelcast..."

%install
rm -rf $RPM_BUILD_ROOT

%{__mkdir} -p %{buildroot}%{_prefix}/lib/%{name}/%{name}-%{hzversion}
%{__cp} -vrf %{name}-%{hzversion}/* %{buildroot}%{_prefix}/lib/%{name}/%{name}-%{hzversion}
%{__chmod} 755 %{buildroot}%{_prefix}/lib/%{name}/%{name}-%{hzversion}/bin/hz*
%{__mkdir} -p %{buildroot}/%{_bindir}

%{__ln_s} %{_prefix}/lib/%{name}/%{name}-%{hzversion}/bin/hz %{buildroot}/%{_bindir}/hz
%{__ln_s} %{_prefix}/lib/%{name}/%{name}-%{hzversion}/bin/hz-cli %{buildroot}/%{_bindir}/hz-cli
%{__ln_s} %{_prefix}/lib/%{name}/%{name}-%{hzversion}/bin/hz-cluster-admin %{buildroot}/%{_bindir}/hz-cluster-admin
%{__ln_s} %{_prefix}/lib/%{name}/%{name}-%{hzversion}/bin/hz-cluster-cp-admin %{buildroot}/%{_bindir}/hz-cluster-cp-admin
%{__ln_s} %{_prefix}/lib/%{name}/%{name}-%{hzversion}/bin/hz-healthcheck %{buildroot}/%{_bindir}/hz-healthcheck
%{__ln_s} %{_prefix}/lib/%{name}/%{name}-%{hzversion}/bin/hz-start %{buildroot}/%{_bindir}/hz-start
%{__ln_s} %{_prefix}/lib/%{name}/%{name}-%{hzversion}/bin/hz-stop %{buildroot}/%{_bindir}/hz-stop

echo 'hazelcastDownloadId=CLI_RPM' > "%{buildroot}%{_prefix}/lib/%{name}/%{name}-%{hzversion}/bin/download/hazelcast-download.properties"

%post
printf "\n\nHazelcast is successfully installed to '%{_prefix}/lib/%{name}/%{name}-%{hzversion}/'\n"
hz --help

%clean
rm -rf $RPM_BUILD_ROOT

%files
%{_prefix}/lib/%{name}/%{name}-%{hzversion}/*
%config(noreplace) %{_prefix}/lib/%{name}/%{name}-%{hzversion}/config/*.xml
%config(noreplace) %{_prefix}/lib/%{name}/%{name}-%{hzversion}/config/*.yaml
%config(noreplace) %{_prefix}/lib/%{name}/%{name}-%{hzversion}/config/*.options
%config(noreplace) %{_prefix}/lib/%{name}/%{name}-%{hzversion}/config/*.properties
%config(noreplace) %{_prefix}/lib/%{name}/%{name}-%{hzversion}/config/examples/*.yaml
%config(noreplace) %{_prefix}/lib/%{name}/%{name}-%{hzversion}/config/examples/*.xml
%{_bindir}/hz
%{_bindir}/hz-cli
%{_bindir}/hz-cluster-admin
%{_bindir}/hz-cluster-cp-admin
%{_bindir}/hz-healthcheck
%{_bindir}/hz-start
%{_bindir}/hz-stop

%changelog
* Mon Nov 02 2020 Devops Hazelcast <devops@hazelcast.com> - 4.2020.11
- Added post installation step for usage printout
* Tue Oct 13 2020 Devops Hazelcast <devops@hazelcast.com> - 4.2020.11
- Mark configuration files for upgrades
* Tue Oct 13 2020 Devops Hazelcast <devops@hazelcast.com> - 4.2020.11
- This is the initial RPM package spec
