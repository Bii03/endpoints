package endpoints.algebra

import scala.language.higherKinds

/**
  * Algebra interface for describing Protobuf entities in requests and responses.
  *
  * {{{
  *   /**
  *     * Describes an HTTP endpoint whose:
  *     *  - request uses verb “GET”,
  *     *  - URL is made of the segment “/user” followed by a `String` segment,
  *     *  - response content type is Protobuf and contains a `User`
  *     */
  *   val example = endpoint(get(path / "user" / segment[UUID]), protobufResponse[User])
  * }}}
  *
  * @group algebras
  */
trait ProtobufEntities extends Endpoints {

  //#request-response-types
  /** Type class defining how to represent the `A` information as a Protobuf request entity */
  type ProtobufRequest[A]

  /** Type class defining how to represent the `A` information as a Protobuf response entity */
  type ProtobufResponse[A]
  //#request-response-types

  /** Defines a `RequestEntity[A]` given an implicit `ProtobufRequest[A]` */
  def protobufRequest[A: ProtobufRequest](docs: Documentation = None): RequestEntity[A]

  /** Defines a `Response[A]` given an implicit `ProtobufResponse[A]` */
  def protobufResponse[A: ProtobufResponse](docs: Documentation = None): Response[A]
}

trait ProtobufEntitiesFromCodec extends ProtobufEntities {

  type ProtobufRequest[A] = Codec[Array[Byte], A]
  type ProtobufResponse[A] = Codec[Array[Byte], A]

  //#json-codec-type
  /** A Protobuf codec type class */
  type ProtobufCodec[A]
  //#json-codec-type

  /** Turns a ProtobufCodec[A] into a Codec[Array[Byte], A] */
  implicit def protobufCodec[A: ProtobufCodec]: Codec[Array[Byte], A]

}
