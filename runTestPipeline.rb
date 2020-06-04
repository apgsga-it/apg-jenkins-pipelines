#!/usr/bin/env ruby
# encoding: utf-8
# TODO (che, 4.6) : Finish this . Missing exec of Gradle Test
require "slop"
opts = Slop.parse do |o|
  o.string '-t', '--task', 'Gradle Test Pipeline Task'
  o.separator ''
  o.separator 'other options:'
  o.string '-i', '--installDir', 'Jenkinsrunner Installation Dir', default: '/opt/jenkinstests'
  o.bool '-l', '--list', 'List available Gradle Pipeline Test Tasks', default: false
  o.on '-h', '--help' do
    puts o
    exit
  end
end
if (!opts[:task] and !opts[:list])
  puts "You need to choose either -t or -l option"
  puts opts
  exit
end

def parseOutput(gradleOutput)
  lines = gradleOutput.split("\n")
  start = 0;
  taskMap = {}
  lines.each do |line|
    start = start + 1 if start >= 1
    start = 1 if line.match(/Apg Gradle/)
    break if line.match(/To see all tasks /)
    if start > 2
      result = line.split(" - ")
      if result.length > 1
        taskMap[result[0].strip] = result[1].strip
      end
    end
  end
  return taskMap
end

def listTasks(taskMap)
  puts "Available Tasks are:"
  taskMap.each do
  | key, value |
    puts "Taskname: #{key} , Description: #{value}"
  end
end

gradleOutput = `./gradlew tasks --group="Apg Gradle Jenkinsrunner"`
taskMap = parseOutput(gradleOutput);
if opts[:list]
  listTasks(taskMap)
end
if opts[:task] and !taskMap.key?(opts[:task])
  puts "#{opts[:task]} unknown task"
  listTasks(taskMap)
  exit
end


