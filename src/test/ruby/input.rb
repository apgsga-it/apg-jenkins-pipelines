#!/usr/bin/env ruby
# encoding: utf-8

require "rubygems"
require "bunny"

STDOUT.sync = true

conn = Bunny.new(:host => "lxewi163.apgsga.ch", :user => "admin", :pass => "test")
conn.start

channel = conn.create_channel
queue = channel.queue("jenkins.tests.input", :auto_delete => true)
recievedMsg = nil
queue.subscribe(manual_ack: true, block: true) do |delivery_info, _, payload|
  recievedMsg = payload
  channel.ack(delivery_info.delivery_tag)
  channel.close
end
puts "Received #{recievedMsg}"
conn.close

# # keeps the main thread alive
# while recievedPayLoad == nil do
#   puts "Looping "
#   sleep 2
# end