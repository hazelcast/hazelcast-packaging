package com.hazelcast.commandline.member;

import com.hazelcast.core.Hazelcast;

public class HazelcastMember {
    public static void main(String[] args) {
        Hazelcast.newHazelcastInstance();
    }
}
