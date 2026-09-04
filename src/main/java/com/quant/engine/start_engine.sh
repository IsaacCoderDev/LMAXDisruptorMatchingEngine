#!/bin/bash

# Target JDK 21 LTS 
JAVA_HOME="/usr/lib/jvm/java-21-openjdk-amd64"

# ==============================================================================
# MEMORY & GARBAGE COLLECTION
# ==============================================================================
GC_FLAGS="-Xms4G -Xmx4G -XX:+UnlockExperimentalVMOptions -XX:+UseEpsilonGC"

# GC_FLAGS="-Xms4G -Xmx4G -XX:+UseZGC -XX:+ZGenerational"

# ==============================================================================
# PERFORMANCE TUNING
# ==============================================================================
PERF_FLAGS="-XX:+AlwaysPreTouch -XX:-UseBiasedLocking -XX:-RestrictContended"

# ==============================================================================
# COMPILER TUNING (JIT)
# ==============================================================================
JIT_FLAGS="-XX:CompileThreshold=1000 -XX:+AggressiveOpts"

taskset -c 1-4 $JAVA_HOME/bin/java $GC_FLAGS $PERF_FLAGS $JIT_FLAGS -cp target/matching-engine-1.0-SNAPSHOT.jar com.quant.engine.MatchingEngineBootstrapper