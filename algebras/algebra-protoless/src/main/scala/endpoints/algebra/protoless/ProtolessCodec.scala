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

import io.protoless.messages.{Decoder => ProtolessDecoder, Encoder => ProtolessEncoder}

/**
  * Combines both an [[io.protoless.messages.Encoder]] and a [[io.protoless.messages.Decoder]] into a single type class.
  *
  * You don’t need to define instances by yourself as they can be derived from an existing pair
  * of an [[io.protoless.messages.Encoder]] and a [[io.protoless.messages.Decoder]].
  *
  */
trait ProtolessCodec[A] {
  def encoder: ProtolessEncoder[A]

  def decoder: ProtolessDecoder[A]
}

object ProtolessCodec {

  def apply[A](implicit codec: ProtolessCodec[A]): ProtolessCodec[A] = codec

  implicit def fromEncoderAndDecoder[A](implicit enc: ProtolessEncoder[A], dec: ProtolessDecoder[A]): ProtolessCodec[A] =
    new ProtolessCodec[A] {
      val encoder: ProtolessEncoder[A] = enc
      val decoder: ProtolessDecoder[A] = dec
    }
}

