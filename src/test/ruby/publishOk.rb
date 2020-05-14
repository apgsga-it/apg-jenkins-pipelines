#!/usr/bin/env ruby
# encoding: utf-8

require "rubygems"
require "bunny"

STDOUT.sync = true

conn = Bunny.new(:host =>  "lxewi163.apgsga.ch", :user => "admin", :pass => "test")
conn.start

ch = conn.create_channel
q  = ch.queue("jenkins.tests.input", :auto_delete => true)
x  = ch.default_exchange

x.publish("Ok!", :routing_key => q.name)

conn.close
