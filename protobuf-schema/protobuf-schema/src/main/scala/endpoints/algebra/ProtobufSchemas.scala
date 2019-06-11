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
package endpoints.algebra

import java.util.UUID

import scala.collection.compat.Factory

/**
  * An algebra interface for describing algebraic data types. Such descriptions
  * can be interpreted to produce a Protobuf schema of the data type, a Protobuf encoder,
  * a Protobuf decoder, etc.
  *
  * A description contains the fields of a case class and their type, and the
  * constructor names of a sealed trait.
  *
  * For instance, consider the following record type:
  *
  * {{{
  *   case class User(name: String, age: Int)
  * }}}
  *
  * Its description is the following:
  *
  * {{{
  *   object User {
  *     implicit val schema: ProtobufSchema[User] = (
  *       field[String]("name") zip
  *       field[Int]("age")
  *     ).xmap((User.apply _).tupled)(Function.unlift(User.unapply))
  *   }
  * }}}
  *
  * The description says that the record type has two fields, the first one has type `String` and is
  * named “name”, and the second one has type `Int` and name “age”.
  *
  * To describe sum types you have to explicitly “tag” each alternative:
  *
  * {{{
  *   sealed trait Shape
  *   case class Circle(radius: Double) extends Shape
  *   case class Rectangle(width: Double, height: Double) extends Shape
  *
  *   object Shape {
  *     implicit val schema: ProtobufSchema[Shape] = {
  *       val circleSchema = field[Double]("radius").xmap(Circle)(Function.unlift(Circle.unapply))
  *       val rectangleSchema = (
  *         field[Double]("width") zip
  *         field[Double]("height")
  *       ).xmap((Rectangle.apply _).tupled)(Function.unlift(Rectangle.unapply))
  *       (circleSchema.tagged("Circle") orElse rectangleSchema.tagged("Rectangle"))
  *         .xmap[Shape] {
  *           case Left(circle) => circle
  *           case Right(rect)  => rect
  *         } {
  *           case c: Circle    => Left(c)
  *           case r: Rectangle => Right(r)
  *         }
  *     }
  *   }
  * }}}
  *
  * @group algebras
  */
trait ProtobufSchemas {
  
  /** The Protobuf schema of a type `A` */
  type ProtobufSchema[A]

  /** The Protobuf schema of a record type (case class) `A` */
  type Record[A] <: ProtobufSchema[A]

  /** A Protobuf schema containing the name of the type `A`.
    * Tagged schemas are useful to describe sum types (sealed traits).
    */
  type Tagged[A] <: ProtobufSchema[A]

  /** A Protobuf schema for enumerations, i.e. types that have a restricted set of values. */
  type Enum[A] <: ProtobufSchema[A]

  /** Promotes a schema to an enumeration and converts between enum constants and Protobuf strings.
    * Decoding fails if the input string does not match the encoded values of any of the possible values.
    * Encoding does never fail, even if the value is not contained in the set of possible values.
    * */
  def enumeration[A](values: Seq[A])(encode: A => String)(implicit tpe: ProtobufSchema[String]): Enum[A]

  /** Annotates Protobuf schema with a name */
  def named[A, S[T] <: ProtobufSchema[T]](schema: S[A], name: String): S[A]

  /**
    * Captures a lazy reference to a Protobuf schema currently being defined:
    *
    * {{{
    *   case class Rec(next: Option[Rec])
    *   val recSchema: ProtobufSchema[Rec] = (
    *     optField("next")(lazySchema(recSchema, "Rec"))
    *   ).xmap(Rec)(_.next)
    * }}}
    *
    * Interpreters should return a ProtobufSchema value that does not evaluate
    * the given `schema` unless it is effectively used.
    *
    * @param schema The Protobuf schema whose evaluation should be delayed
    * @param name A unique name identifying the schema
    */
  def lazySchema[A](schema: => ProtobufSchema[A], name: String): ProtobufSchema[A]

  /** The Protobuf schema of a record with no fields */
  def emptyRecord: Record[Unit]

  /** The Protobuf schema of a record with a single field `name` of type `A` */
  def field[A](name: String, documentation: Option[String] = None)(implicit tpe: ProtobufSchema[A]): Record[A]

  /** The Protobuf schema of a record with a single optional field `name` of type `A` */
  def optField[A](name: String, documentation: Option[String] = None)(implicit tpe: ProtobufSchema[A]): Record[Option[A]]

  /** Tags a schema for type `A` with the given tag name */
  def taggedRecord[A](recordA: Record[A], tag: String): Tagged[A]

  /** Default discriminator field name for sum types.
    *
    * It defaults to "type", but you can override it twofold:
    * - by overriding this field you can change default discriminator name algebra-wide
    * - by using `withDiscriminator` you can specify discriminator field name for specific sum type
    * */
  def defaultDiscriminatorName: String = "type"

  /** Allows to specify name of discriminator field for sum type */
  def withDiscriminator[A](tagged: Tagged[A], discriminatorName: String): Tagged[A]

  /** The Protobuf schema of a coproduct made of the given alternative tagged records */
  def choiceTagged[A, B](taggedA: Tagged[A], taggedB: Tagged[B]): Tagged[Either[A, B]]

  /** The Protobuf schema of a record merging the fields of the two given records */
  def zipRecords[A, B](recordA: Record[A], recordB: Record[B]): Record[(A, B)]

  /** Transforms the type of the Protobuf schema */
  def xmapRecord[A, B](record: Record[A], f: A => B, g: B => A): Record[B]

  /** Transforms the type of the Protobuf schema */
  def xmapTagged[A, B](taggedA: Tagged[A], f: A => B, g: B => A): Tagged[B]

  /** Transforms the type of the Protobuf schema */
  def xmapProtobufSchema[A, B](jsonSchema: ProtobufSchema[A], f: A => B, g: B => A): ProtobufSchema[B]

  /** Convenient infix operations */
  final implicit class RecordOps[A](recordA: Record[A]) {
    def zip[B](recordB: Record[B]): Record[(A, B)] = zipRecords(recordA, recordB)
    def xmap[B](f: A => B)(g: B => A): Record[B] = xmapRecord(recordA, f, g)
    def tagged(tag: String): Tagged[A] = taggedRecord(recordA, tag)
  }

  /** Convenient infix operations */
  final implicit class ProtobufSchemaOps[A](protobufSchema: ProtobufSchema[A]) {
    def xmap[B](f: A => B)(g: B => A): ProtobufSchema[B] = xmapProtobufSchema(protobufSchema, f, g)
  }

  final implicit class TaggedOps[A](taggedA: Tagged[A]) {
    def orElse[B](taggedB: Tagged[B]): Tagged[Either[A, B]] = choiceTagged(taggedA, taggedB)
    def xmap[B](f: A => B)(g: B => A): Tagged[B] = xmapTagged(taggedA, f, g)
  }

  /** A Protobuf schema for type `UUID` */
  implicit def uuidProtobufSchema: ProtobufSchema[UUID]

  /** A Protobuf schema for type `String` */
  implicit def stringProtobufSchema: ProtobufSchema[String]

  /** A Protobuf schema for type `Int` */
  implicit def intProtobufSchema: ProtobufSchema[Int]

  /** A Protobuf schema for type `Long` */
  implicit def longProtobufSchema: ProtobufSchema[Long]

  /** A Protobuf schema for type `BigDecimal` */
  implicit def bigdecimalProtobufSchema: ProtobufSchema[BigDecimal]

  /** A Protobuf schema for type `Float` */
  implicit def floatProtobufSchema: ProtobufSchema[Float]

  /** A Protobuf schema for type `Double` */
  implicit def doubleProtobufSchema: ProtobufSchema[Double]

  /** A Protobuf schema for type `Boolean` */
  implicit def booleanProtobufSchema: ProtobufSchema[Boolean]

  /** A Protobuf schema for sequences */
  implicit def arrayProtobufSchema[C[X] <: Seq[X], A](implicit
    jsonSchema: ProtobufSchema[A],
    factory: Factory[A, C[A]]
  ): ProtobufSchema[C[A]]

  /** A Protobuf schema for maps with string keys */
  implicit def mapProtobufSchema[A](implicit protobufSchema: ProtobufSchema[A]): ProtobufSchema[Map[String, A]]
  
}
