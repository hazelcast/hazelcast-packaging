%define hzversion ${HZ_VERSION}
%define hzdistribution ${HZ_DISTRIBUTION}

Name:       %{hzdistribution}
Version:    ${RPM_PACKAGE_VERSION}
Epoch:      1
Release:    1
Summary:    A tool that allows users to install & run Hazelcast

License:    ASL 2.0
URL:		https://hazelcast.org/

Source0:    %{hzdistribution}-%{hzversion}.tar.gz

Requires:	java-1.8.0-devel

BuildArch:  noarch

%description
A tool that allows users to install & run Hazelcast

%prep
%setup -c %{name}-%{hzversion}

%build
true

%pre
echo "Installing Hazelcast..."

%install
rm -rf $RPM_BUILD_ROOT

%{__mkdir} -p %{buildroot}%{_prefix}/lib/hazelcast
%{__cp} -vrf %{name}-%{hzversion}/* %{buildroot}%{_prefix}/lib/hazelcast
%{__chmod} 755 %{buildroot}%{_prefix}/lib/hazelcast/bin/hz*
%{__mkdir} -p %{buildroot}/%{_bindir}

%{__ln_s} %{_prefix}/lib/hazelcast/bin/hz %{buildroot}/%{_bindir}/hz
%{__ln_s} %{_prefix}/lib/hazelcast/bin/hz-cli %{buildroot}/%{_bindir}/hz-cli
%{__ln_s} %{_prefix}/lib/hazelcast/bin/hz-cluster-admin %{buildroot}/%{_bindir}/hz-cluster-admin
%{__ln_s} %{_prefix}/lib/hazelcast/bin/hz-cluster-cp-admin %{buildroot}/%{_bindir}/hz-cluster-cp-admin
%{__ln_s} %{_prefix}/lib/hazelcast/bin/hz-healthcheck %{buildroot}/%{_bindir}/hz-healthcheck
%{__ln_s} %{_prefix}/lib/hazelcast/bin/hz-start %{buildroot}/%{_bindir}/hz-start
%{__ln_s} %{_prefix}/lib/hazelcast/bin/hz-stop %{buildroot}/%{_bindir}/hz-stop

echo 'hazelcastDownloadId=rpm' > "%{buildroot}%{_prefix}/lib/hazelcast/lib/hazelcast-download.properties"

%post
printf "\n\nHazelcast is successfully installed to '%{_prefix}/lib/hazelcast/'\n"
hz --help

%clean
rm -rf $RPM_BUILD_ROOT

%preun
echo "Removing symlinks from %{_bindir}"

for FILENAME in %{_prefix}/lib/hazelcast/bin/hz*; do
  case "${FILENAME}" in
    *bat)
      ;;
    *)
      echo "Remove ${filename}"
      rm %{_bindir}/"$(basename "${FILENAME}")"
      ;;
  esac
done

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
