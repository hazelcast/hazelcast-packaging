%define hzversion ${HZ_VERSION}
%define hzdistribution ${HZ_DISTRIBUTION}

Name:       %{hzdistribution}
Version:    ${RPM_PACKAGE_VERSION}
Epoch:      1
Release:    1
Summary:    A tool that allows users to install & run Hazelcast and Management Center on the local environment

License:    ASL 2.0
URL:		https://hazelcast.org/

Source0:    %{hzdistribution}-%{hzversion}.tar.gz

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

echo 'hazelcastDownloadId=rpm' > "%{buildroot}%{_prefix}/lib/%{name}/%{name}-%{hzversion}/lib/hazelcast-download.properties"

%post
printf "\n\nHazelcast is successfully installed to '%{_prefix}/lib/%{name}/%{name}-%{hzversion}/'\n"
hz --help

%clean
rm -rf $RPM_BUILD_ROOT

%postun
echo "Removing symlinks from /usr/bin"

for FILENAME in /usr/lib/hazelcast/${HZ_DISTRIBUTION}-${HZ_VERSION}/bin/hz*; do
  case "${FILENAME}" in
    *bat)
      ;;
    *)
      rm "$(basename "${FILENAME}")"
      ;;
  esac
done

if [  ! -f %{buildroot}/%{_bindir}/hz  ]; then
    rm %{buildroot}/%{_bindir}/hz
    rm %{buildroot}/%{_bindir}/hz-cli
    rm %{buildroot}/%{_bindir}/hz-cluster-admin
    rm %{buildroot}/%{_bindir}/hz-cluster-cp-admin
    rm %{buildroot}/%{_bindir}/hz-healthcheck
    rm %{buildroot}/%{_bindir}/hz-start
    rm %{buildroot}/%{_bindir}/hz-stop
fi

%files
# The LICENSE file contains Apache 2 license and is only present in OS
%if "%{hzdistribution}" == "hazelcast"
   %{_prefix}/lib/%{name}/%{name}-%{hzversion}/LICENSE
%endif
%{_prefix}/lib/%{name}/%{name}-%{hzversion}/NOTICE
%{_prefix}/lib/%{name}/%{name}-%{hzversion}/bin
%{_prefix}/lib/%{name}/%{name}-%{hzversion}/custom-lib
%{_prefix}/lib/%{name}/%{name}-%{hzversion}/lib
%{_prefix}/lib/%{name}/%{name}-%{hzversion}/licenses
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
