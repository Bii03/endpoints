package endpoints.xhr.future

import endpoints.xhr

import scala.concurrent.{Future, Promise}

/**
  * Implements [[xhr.Endpoints]] by using Scala’s [[Future]]s.
  */
trait Endpoints extends xhr.Endpoints {

  /** Maps `Task` to [[Future]] */
  type Task[A] = Future[A]

  def endpoint[A, B](request: Request[A], response: Response[B]): Endpoint[A, B] =
    (a: A) => {
      val promise = Promise[B]()
      performXhr(request, response, a)(
        _.fold(exn => { promise.failure(exn); () }, b => { promise.success(b); () }),
        xhr => { promise.failure(new Exception(xhr.responseText)); () }
      )
      promise.future
    }

}