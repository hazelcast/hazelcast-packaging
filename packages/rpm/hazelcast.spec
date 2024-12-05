%define hzversion ${HZ_VERSION}
%define rpmhzversion ${RPM_HZ_VERSION}
%define releaseversion ${RELEASE_VERSION}
%define hzdistribution ${HZ_DISTRIBUTION}
%define debug_package %{nil}

Name:       %{hzdistribution}
Version:    %{rpmhzversion}
Epoch:      1
Release:    %{releaseversion}
Summary:    Hazelcast is a streaming and memory-first application platform.

License:    ASL 2.0
URL:		https://hazelcast.org/

Source0:    %{hzdistribution}-%{hzversion}.tar.gz
Source1:    hazelcast.service

Requires(pre): shadow-utils

Requires:	java-21

BuildArch:  noarch
BuildRequires: systemd-rpm-macros

%description
Hazelcast is a streaming and memory-first application platform for fast, stateful, data-intensive workloads
on-premises, at the edge or as a fully managed cloud service.

%prep
%setup -c %{name}-%{hzversion}

%build
true

%pre
echo "Installing Hazelcast..."

# See https://fedoraproject.org/wiki/Packaging%3aUsersAndGroups#Dynamic_allocation
getent group hazelcast >/dev/null || groupadd -r hazelcast
getent passwd hazelcast >/dev/null || \
    useradd -r -g hazelcast -d %{_prefix}/lib/hazelcast -s /sbin/nologin \
    -c "User to run server process of Hazelcast" hazelcast

%install
rm -rf $RPM_BUILD_ROOT

%{__mkdir} -p %{buildroot}%{_prefix}/lib/hazelcast
%{__cp} -vrf %{name}-%{hzversion}/* %{buildroot}%{_prefix}/lib/hazelcast
%{__chmod} 755 %{buildroot}%{_prefix}/lib/hazelcast/bin/hz*
%{__mkdir} -p %{buildroot}%{_bindir}

%{__mkdir} -p %{buildroot}%{_unitdir}
%{__cp} %{SOURCE1} %{buildroot}%{_unitdir}/hazelcast.service

%{__ln_s} %{_prefix}/lib/hazelcast/bin/hz %{buildroot}%{_bindir}/hz
%{__ln_s} %{_prefix}/lib/hazelcast/bin/hz-cli %{buildroot}%{_bindir}/hz-cli
%{__ln_s} %{_prefix}/lib/hazelcast/bin/hz-cluster-admin %{buildroot}%{_bindir}/hz-cluster-admin
%{__ln_s} %{_prefix}/lib/hazelcast/bin/hz-cluster-cp-admin %{buildroot}%{_bindir}/hz-cluster-cp-admin
%{__ln_s} %{_prefix}/lib/hazelcast/bin/hz-healthcheck %{buildroot}%{_bindir}/hz-healthcheck
%{__ln_s} %{_prefix}/lib/hazelcast/bin/hz-start %{buildroot}%{_bindir}/hz-start
%{__ln_s} %{_prefix}/lib/hazelcast/bin/hz-stop %{buildroot}%{_bindir}/hz-stop
# Fix for missing custom-lib
%{__mkdir} -p %{buildroot}%{_prefix}/lib/hazelcast/custom-lib

echo 'hazelcastDownloadId=rpm' > "%{buildroot}%{_prefix}/lib/hazelcast/lib/hazelcast-download.properties"

%clean
rm -rf $RPM_BUILD_ROOT

%post
find %{_prefix}/lib/hazelcast -not -path "*/logs/*" -exec chown hazelcast:hazelcast {} \;
mkdir -p %{_prefix}/lib/hazelcast/logs
chmod 777 %{_prefix}/lib/hazelcast/logs
%systemd_post %{name}.service
printf "\n\nHazelcast is successfully installed to '%{_prefix}/lib/hazelcast/'\n"
printf "\n\nUse 'hz start' or 'systemctl start hazelcast' to start the Hazelcast server\n"

%preun
%systemd_preun %{name}.service

%postun
%systemd_postun %{name}.service

%files
# The LICENSE file contains Apache 2 license and is only present in OS
%if "%{hzdistribution}" == "hazelcast"
   %{_prefix}/lib/hazelcast/LICENSE
%endif
%{_prefix}/lib/hazelcast/NOTICE
%{_prefix}/lib/hazelcast/bin
%{_prefix}/lib/hazelcast/custom-lib
%{_prefix}/lib/hazelcast/lib
%{_prefix}/lib/hazelcast/licenses
%{_prefix}/lib/hazelcast/release_notes.txt
%config(noreplace) %{_prefix}/lib/hazelcast/config/*.xml
%config(noreplace) %{_prefix}/lib/hazelcast/config/*.yaml
%config(noreplace) %{_prefix}/lib/hazelcast/config/*.options
%config(noreplace) %{_prefix}/lib/hazelcast/config/*.properties
%config(noreplace) %{_prefix}/lib/hazelcast/config/examples/*.yaml
%config(noreplace) %{_prefix}/lib/hazelcast/config/examples/*.xml
%{_bindir}/hz
%{_bindir}/hz-cli
%{_bindir}/hz-cluster-admin
%{_bindir}/hz-cluster-cp-admin
%{_bindir}/hz-healthcheck
%{_bindir}/hz-start
%{_bindir}/hz-stop
%{_unitdir}/hazelcast.service
