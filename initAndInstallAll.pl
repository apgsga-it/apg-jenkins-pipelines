#!/usr/bin/perl
use strict;
use warnings FATAL => 'all';
use Getopt::Long;
use Pod::Usage;

my $installDir="/opt/jenkinstests";
my $user;
my $man = 0;
my $help = 0;
my $remove = 0;
GetOptions(
    'help'       => \$help,
    'man'        => \$man,
    'user=s'     => \$user,
    'installDir=s' => \$installDir,
    'remove' => \$remove
) or pod2usage(2);

pod2usage(-exitval => 0, -verbose => 2) if $man;
if (!$user)
{
    print("User argument missing","\n");
    pod2usage(1);
}
print "Running with Target directory=$installDir, user:$user", "\n";
print "Not Removeing Installation Dir: $installDir" if !$remove;
if ($remove && -e $installDir) {
    print "Removeing Installation Dir:  $installDir recursively", "\n";
    system "sudo rm -Rf  $installDir"
}
if  (!-e $installDir) {
    print "Creating Installation Dir:  $installDir", "\n";
    system "sudo mkdir $installDir";
    print "Changing Owner of  Installation Dir:  $installDir", "\n";
    my $chown = "sudo chown -R ${\trim(`id -u`)}:${\trim(`id -g`)} $installDir";
    print "Executing : $chown", "\n";
    system $chown
}
system "./initLocalRepos.sh -u $user -i $installDir";
system "./initLocalRepos.sh -u $user -i $installDir";
system "./installJenkinsFilerunner.sh -i $installDir";
system "./installApscli.sh -i $installDir";
system "./runJenkinsPipeline.sh -i $installDir -a";

sub  trim { my $s = shift; $s =~ s/^\s+|\s+$//g; return $s };

__END__

=head1 NAME

 initAndInstallAll.pl

=head1 SYNOPSIS

    initAndInstallAll.pl -u USER [ -i INSTALL_DIR ] [-r]

=head1 DESCRIPTION

    Runs all installation and initialization scripts for Jenkinspipeline Tests:
    - initLocalRepos.sh : Initialization of a Gradle User Home and Mavenlocal with USER
    - installJenkinsFilerunner.sh : Installs the Jenkins Filerunner
    - installApscli.sh : Installs the serverless apscli needed for apg Patch Pipelines
    - testJenkinsPipeline.sh : Tests the Installations


=head1 ARGUMENTS

    -h|help          display this help and exit
    -u|user=USER     userid for the gitrepo, from which apg-gradle-properties will be clone. Mandatory option
    -i|installDir=INSTALL_DIR Installation Directory , defaults to "/opt/jenkinstests"
    -r|remove        removes the INSTALL_DIR before installation, defaults to false

=head1 AUTHOR

 chhex

=head1 CREDITS

=head1 TESTED

=head1 BUGS

 None that I know of.

=head1 TODO


=head1 UPDATES


=cut

