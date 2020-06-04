#!/usr/bin/env ruby
# encoding: utf-8

require "bunny"

STDOUT.sync = true

conn = Bunny.new(:host =>  "lxewi163.apgsga.ch", :user => "admin", :pass => "test")
conn.start

ch = conn.create_channel
q  = ch.queue("bunny.examples.hello_world", :auto_delete => true)
x  = ch.default_exchange

q.subscribe do |delivery_info, metadata, payload|
  puts "Received #{payload}"
end

x.publish("Hello!", :routing_key => q.name)

sleep 1.0
conn.close
