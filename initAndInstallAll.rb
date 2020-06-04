#!/usr/bin/env ruby
# encoding: utf-8
require "slop"

opts = Slop.parse do |o|
  o.string '-u', '--username', '--user', 'Apg git user'
  o.separator ''
  o.separator 'other options:'
  o.string '-i', '--installDir', 'Jenkinsrunner Installation Dir', default: '/opt/jenkinstests'
  o.bool '-r', '--remove', 'Installation will be removed first, default false', default: false
  o.bool '-s', '--skipLocalRepo', 'Skip Initialization of Local Repos ', default: false
  o.bool '-x', '--skipFileRunner', 'Skip Installation of Jenkins File Runner ', default: false
  o.bool '-n', '--skipApsCli', 'Skip Installation of Aps serverless Cli ', default: false
  o.on '-h', '--help' do
    puts o
    exit
  end
end

if !opts[:user]
  puts "Username must be supplied"
  puts opts
  exit
end

if opts[:remove] and (opts[:skipLocalRepo] || opts[:skipFileRunner] ||  opts[:skipApsCli])
  puts "Removing installation directory  #{opts[:installDir]} contradicts skip options  "
  puts opts
  exit
end

puts "Running with User: #{opts[:user]}, remove: #{opts[:remove]}, installation Directory: #{opts[:installDir]}"

if opts[:remove] && Dir.exist?(opts[:installDir])
  puts "Deleting Installation Dir:  #{opts[:installDir]} recursively with sudo"
  system("sudo rm -Rf  #{opts[:installDir]}")
  puts "done."
end

if !Dir.exist?(opts[:installDir])
  puts "Creating Installation Dir:  #{opts[:installDir]}"
  system("sudo mkdir #{opts[:installDir]}")
  puts "Changing Owner of  Installation Dir: #{opts[:installDir]} to current user: #{opts[:user]}"
  userId = `id -u`.strip
  userGrpId = `id -g`.strip
  chownCmd = "sudo chown -R #{userId}:#{userGrpId} #{opts[:installDir]}"
  puts "Executing : #{chownCmd}", "\n";
  system(chownCmd)
end
if !opts[:skipLocalRepo]
  system("./initLocalRepos.sh -u #{opts[:user]} -i #{opts[:installDir]}")
end
if !opts[:skipFileRunner]
  system("./installJenkinsFilerunner.sh -i #{opts[:installDir]}")
end
if !opts[:skipApsCli]
  system("./installApscli.sh -i #{opts[:installDir]}")
end
system("./runTestPipeline.pl -i #{opts[:installDir]} -t runTestLibHelloWorld")
