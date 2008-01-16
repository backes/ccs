#include "BCGWriter.h"
#include <bcg_user.h>

JNIEXPORT void JNICALL Java_de_unisb_cs_depend_ccs_1sem_exporters_bcg_BCGWriter_open0
  (JNIEnv *env, jclass, jstring filename, jint initialStateNr)
{
  BCG_INIT();
  char* filenameChar = const_cast<char*>(env->GetStringUTFChars(filename, 0));
  BCG_IO_WRITE_BCG_BEGIN(filenameChar, initialStateNr, 2, "", 0);
  env->ReleaseStringUTFChars(filename, filenameChar);
}

JNIEXPORT void JNICALL Java_de_unisb_cs_depend_ccs_1sem_exporters_bcg_BCGWriter_writeTransition0
  (JNIEnv *env, jclass, jint sourceStateNr, jint targetStateNr, jstring label)
{
  char* labelChar = const_cast<char*>(env->GetStringUTFChars(label, 0));
  BCG_IO_WRITE_BCG_EDGE(sourceStateNr, labelChar, targetStateNr);
  env->ReleaseStringUTFChars(label, labelChar);
}

JNIEXPORT void JNICALL Java_de_unisb_cs_depend_ccs_1sem_exporters_bcg_BCGWriter_close0
  (JNIEnv *, jclass)
{
  BCG_IO_WRITE_BCG_END();
}

