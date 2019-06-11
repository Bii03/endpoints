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
package endpoints.algebra.protoless

import endpoints.algebra.{Codec, Decoder, Encoder}

import io.protoless.messages.{Decoder => ProtolessDecoder, Encoder => ProtolessEncoder}

trait ProtobufEntitiesFromCodec extends endpoints.algebra.ProtobufEntitiesFromCodec {

  //#type-carrier
  type ProtobufCodec[A] = ProtolessCodec[A]
  //#type-carrier

  implicit def protobufCodec[A](implicit codec: ProtolessCodec[A]): Codec[Array[Byte], A] = new Codec[Array[Byte], A] {
    override def decode(from: Array[Byte]): Either[Exception, A] = codec.decoder.decode(from)

    override def encode(from: A): Array[Byte] = codec.encoder.encodeAsBytes(from)
  }

  implicit def protolessDecoderToDecoder[A](implicit decoder: ProtolessDecoder[A]): Decoder[Array[Byte], A] =
    new Decoder[Array[Byte], A] {
      def decode(from: Array[Byte]): Either[Exception, A] = decoder.decode(from)
    }

  implicit def protolessEncoderToEncoder[A](implicit encoder: ProtolessEncoder[A]): Encoder[A, Array[Byte]] =
    new Encoder[A, Array[Byte]] {
      def encode(from: A): Array[Byte] = encoder(from)
    }

}
