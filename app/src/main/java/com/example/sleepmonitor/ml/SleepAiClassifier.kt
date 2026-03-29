package com.example.sleepmonitor.ml

/**
 * Placeholder kept to preserve the repository structure under `app/` + `ml/`.
 *
 * The shipped Android artifact no longer bundles the TensorFlow Lite JNI runtime
 * because the packaged `libtensorflowlite_jni.so` was only 4 KB ELF aligned and
 * triggered the 16 KB page size compatibility warning in Google Play.
 */
object SleepAiClassifier
