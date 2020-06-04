#!/usr/bin/perl
use strict;
use warnings FATAL => 'all';
use Getopt::Long;
use Pod::Usage;

my $installDir = "/opt/jenkinstests";
my $jenkinsRunnerDir = "$installDir/runner";
my $mavenBaseDir = "$installDir/maven";
my $gradleHomeDir = "$installDir/gradle/home";
my $listTasks = 0;
my $gradleTask = "";
my $man = 0;
my $help = 0;
GetOptions(
    'help'         => \$help,
    'man'          => \$man,
    'task=s'       => \$gradleTask,
    'list'         => \$listTasks,
    'installDir=s' => \$installDir
) or pod2usage(2);
pod2usage(-exitval => 0) if $help or (!$man && !$gradleTask && !$listTasks);
pod2usage(-exitval => 0, -verbose => 2) if $man;
my $gradleOutput =`./gradlew tasks --group="Apg Gradle Jenkinsrunner"`;
my %tasks = parseOutput($gradleOutput);
if ($listTasks) {
    my @task = keys %tasks;
    foreach (@task) {
        print "Taskname: $_ ,  description: $tasks{$_}", "\n";
    }
}
die "Task: $gradleTask doesn't exist.\nAvailable tasks:\n${\taskNames(\%tasks)}\nExiting" if ($gradleTask && ! exists($tasks{$gradleTask}));
system "./gradlew $gradleTask -PinstallDir=$jenkinsRunnerDir -PmavenSettings=$mavenBaseDir/settings.xml -Dgradle.user.home=$gradleHomeDir --info --stacktrace" if ($gradleTask);

sub taskNames {
    return join "\n" , keys shift;
}
sub trim {
    my $s = shift;
    $s =~ s/^\s+|\s+$//g;
    return $s
}

sub parseOutput {
    my $output = shift;
    my $start = 0;
    my %tasks = ();
    for my $line (split /\n/, $output) {
        $start++ if ($start ge 1);
        last if ($line =~ /To see all tasks /);
        $start = 1 if ($line =~ /Apg Gradle/);
        if ($start > 2) {
            my ($name, $desc) = split(/-/, $line);
            $tasks{trim($name)} = $desc if ($name);
        }
    }
    return %tasks
}

__END__

=head1 NAME

 runJenkinsPipeline.pl

=head1 SYNOPSIS

    initAndInstallAll.pl [-list] [-t TASKNAME] [ -i INSTALL_DIR ] [-h] [-m]

=head1 DESCRIPTION

    Runs a specific gradle Task with TASKNAME setting with enviroment specific parameters
    Lists the available Gradle Tasks



=head1 ARGUMENTS

    -h|help          display this help and exits
    -m|man           displays the manual pages and exits
    -i|installDir=INSTALL_DIR Installation Directory , defaults to "/opt/jenkinstests"
    -t|task=TASKNAME runs the Gradle Task with name TASKNAME
    -l|list          list the avaible Gradle Pipeline Testtasks

=head1 AUTHOR

 chhex

=head1 CREDITS

=head1 TESTED

=head1 BUGS

=head1 TODO


=head1 UPDATES


=cut
