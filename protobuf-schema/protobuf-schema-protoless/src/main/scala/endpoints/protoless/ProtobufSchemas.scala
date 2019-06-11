/** ***********************************************************************
  * ADOBE CONFIDENTIAL
  * ___________________
  *
  * Copyright 2018 Adobe Systems Incorporated
  * All Rights Reserved.
  *
  * NOTICE:  All information contained herein is, and remains
  * the property of Adobe Systems Incorporated and its suppliers,
  * if any.  The intellectual and technical concepts contained
  * herein are proprietary to Adobe Systems Incorporated and its
  * suppliers and are protected by all applicable intellectual property
  * laws, including trade secret and copyright laws.
  * Dissemination of this information or reproduction of this material
  * is strictly forbidden unless prior written permission is obtained
  * from Adobe Systems Incorporated.
  * *************************************************************************/
package endpoints
package protoless

import io.protoless.messages.{Decoder, Encoder}

/**
  * An interpreter for [[endpoints.algebra.ProtobufSchemas]] that produces a Protoless codec.
  */
trait ProtobufSchemas
  extends algebra.ProtobufSchemas {

  trait ProtobufSchema[A] {
    def encoder: Encoder[A]
    def decoder: Decoder[A]
  }

  object ProtobufSchema {
    def apply[A](_encoder: Encoder[A], _decoder: Decoder[A]): ProtobufSchema[A] =
      new ProtobufSchema[A] {
        override def encoder: Encoder[A] = _encoder

        override def decoder: Decoder[A] = _decoder
      }

    implicit def toProtolessCodec[A](implicit protobufSchema: ProtobufSchema[A]): ProtolessCodec[A]
  }

}
