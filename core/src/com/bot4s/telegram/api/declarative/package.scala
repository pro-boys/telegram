package com.bot4s.telegram.api

import cats.Applicative

package object declarative {
  type Action[F[_], T] = T => F[Unit]
  type Filter[T] = T => Boolean

  type Args = Seq[String]
  type ActionWithArgs[F[_], T] = T => Args => F[Unit]
  type Extractor[T, R] = T => Option[R]

  /**
    * Adds a filter to an action handler.
    *
    * {{{
    *   when(onCommand('secret), isSenderAuthenticated) {
    *     implicit msg =>
    *       reply("42")
    *   }
    * }}}
    *
    * @param actionInstaller e.g onMessage, onCommand('hello)
    * @param action Action executed if the filter pass.
    *
    */
   def when[F[_]: Applicative, T](actionInstaller: Action[F, T] => Unit, filter: Filter[T])(action: Action[F, T]): Unit = {
    val newAction = {
      t: T =>
        if (filter(t)) {
          action(t)
        } else {
          Applicative[F].pure(())
        }
    }
    actionInstaller(newAction)
  }

  /**
    * Adds a filter to an action handler; including a fallback action.
    *
    * {{{
    *   whenOrElse(onCommand('secret), isSenderAuthenticated) {
    *     implicit msg =>
    *       reply("42")
    *   } /* or else */ {
    *     reply("You must /login first")(_)
    *   }
    * }}}
    *
    * @param actionInstaller e.g onMessage, onCommand('hello)
    * @param action Action executed if the filter pass.
    * @param elseAction Action executed if the filter does not pass.
    */
  def whenOrElse[F[_], T](actionInstaller: Action[F, T] => Unit, filter: Filter[T])
                         (action: Action[F, T])(elseAction: Action[F, T]): Unit = {
    val newAction = {
      t: T =>
        if (filter(t))
          action(t)
        else
          elseAction(t)
    }
    actionInstaller(newAction)
  }
}
