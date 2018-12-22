/* 
 * Copyright (C) 2007, 2008 Wayne Meissner
 * 
 * This file is part of jffi.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * 
 * Alternatively, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */

#include <stdio.h>
#include <stdlib.h>
#if defined(__sun) || defined(_AIX)
# include <alloca.h>
#endif
#include <stdint.h>
#include <stdbool.h>
#include <jni.h>

#include "jffi.h"
#include "Exception.h"
#include "com_kenai_jffi_Foreign.h"
#include <locale.h>

static void 
jffi_encodeLongDouble(JNIEnv *env, long double ld, jbyteArray array, jint arrayOffset, jint arrayLength)
{
    if (arrayLength != sizeof(ld)) {
        throwException(env, Runtime, "array size != sizeof(long double)");
        return;
    }
    (*env)->SetByteArrayRegion(env, array, arrayOffset, arrayLength, (jbyte *) &ld);
}

static long double
jffi_decodeLongDouble(JNIEnv *env, jbyteArray array, jint arrayOffset, jint arrayLength)
{
    long double ld;
    if (arrayLength != sizeof(ld)) {
        throwException(env, Runtime, "array size != sizeof(long double)");
        return 0.0;
    }
    (*env)->GetByteArrayRegion(env, array, arrayOffset, arrayLength, (jbyte *) &ld);
    return ld;
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    longDoubleFromEngineeringString
 * Signature: (Ljava/lang/String;[BII)V
 */
JNIEXPORT void JNICALL 
Java_com_kenai_jffi_Foreign_longDoubleFromString(JNIEnv *env, jobject self, jstring str, 
  jbyteArray array, jint arrayOffset, jint arrayLength)
{
    long double ld;
    char* tmp;
    jsize len;
  
    len = (*env)->GetStringUTFLength(env, str);
    tmp = alloca(len + 1);
    (*env)->GetStringUTFRegion(env, str, 0, len, tmp);
    locale_t myLocale = newlocale (LC_ALL, "C", (locale_t) 0);
    locale_t old_locale = uselocale(myLocale);
    ld = strtold(tmp, NULL);
    uselocale(old_locale);
    freelocale(myLocale);
    jffi_encodeLongDouble(env, ld, array, arrayOffset, arrayLength);
}

static inline jstring
jffi_longDoubleToString(JNIEnv *env, 
                        jbyteArray array, jint arrayOffset, jint arrayLength, const char * const fmt)
{
    char tmp[256];

    locale_t myLocale = newlocale (LC_ALL, "C", (locale_t) 0);
    locale_t old_locale = uselocale(myLocale);
    sprintf(tmp, fmt, jffi_decodeLongDouble(env, array, arrayOffset, arrayLength));
    uselocale(old_locale);
    freelocale(myLocale);
    return (*env)->NewStringUTF(env, tmp);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    longDoubleToEngineeringString
 * Signature: ([BII)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL 
Java_com_kenai_jffi_Foreign_longDoubleToEngineeringString(JNIEnv *env, jobject self, 
    jbyteArray array, jint arrayOffset, jint arrayLength)
{
    return jffi_longDoubleToString(env, array, arrayOffset, arrayLength, "%.60Le");
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    longDoubleToPlainString
 * Signature: ([BII)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL 
Java_com_kenai_jffi_Foreign_longDoubleToPlainString(JNIEnv *env, jobject self, 
    jbyteArray array, jint arrayOffset, jint arrayLength)
{
    return jffi_longDoubleToString(env, array, arrayOffset, arrayLength, "%.60Le");
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    longDoubleToString
 * Signature: ([BII)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL 
Java_com_kenai_jffi_Foreign_longDoubleToString(JNIEnv *env, jobject self, 
    jbyteArray array, jint arrayOffset, jint arrayLength)
{
    return jffi_longDoubleToString(env, array, arrayOffset, arrayLength, "%.60Lg");
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    longDoubleFromDouble
 * Signature: (D[BII)V
 */
JNIEXPORT void JNICALL 
Java_com_kenai_jffi_Foreign_longDoubleFromDouble(JNIEnv *env, jobject self, jdouble doubleValue, 
    jbyteArray array, jint arrayOffset, jint arrayLength)
{
    jffi_encodeLongDouble(env, doubleValue, array, arrayOffset, arrayLength);
}

/*
 * Class:     com_kenai_jffi_Foreign
 * Method:    longDoubleToDouble
 * Signature: ([BII)D
 */
JNIEXPORT jdouble JNICALL 
Java_com_kenai_jffi_Foreign_longDoubleToDouble(JNIEnv *env, jobject self, 
    jbyteArray array, jint arrayOffset, jint arrayLength)
{
    return jffi_decodeLongDouble(env, array, arrayOffset, arrayLength);
}

