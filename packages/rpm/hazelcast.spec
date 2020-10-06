%define hzversion 4.1-BETA-1

Name:		hazelcast
Version:    4.2020.1
Release:	1
Summary:	A tool that allows users to install & run Hazelcast IMDG and Management Center on the local environment

License:	ASL 2.0
URL:		https://hazelcast.org/

Source0:    hazelcast-%{hzversion}.tar.gz

Requires:	(java-1.8.0-devel or java-11-devel)

BuildArch:  noarch

%description
A tool that allows users to install & run Hazelcast IMDG and Management Center on the local environment

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
%{__chmod} 755 %{buildroot}%{_prefix}/lib/%{name}/%{name}-%{hzversion}/bin/hz
%{__mkdir} -p %{buildroot}/%{_bindir}
%{__ln_s} %{_prefix}/lib/%{name}/%{name}-%{hzversion}/bin/hz %{buildroot}/%{_bindir}/hz

%post
echo "Hazelcast is installed successfully."
hz --help

%clean
rm -rf $RPM_BUILD_ROOT

# List of files that this package installs on the system
%files
%{_prefix}/lib/%{name}/%{name}-%{hzversion}/*
%{_bindir}/hz

%changelog
* Wed Sep 9 2020 Alparslan Avci <alparslan@hazelcast.com> - 1.0
- This is the first version
