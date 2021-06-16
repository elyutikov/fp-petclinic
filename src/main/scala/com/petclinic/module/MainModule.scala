package com.petclinic.module

import cats.{Applicative, ApplicativeThrow, Functor, Monad, MonadThrow}
import cats.effect.{Clock, Concurrent, ContextShift, Sync, Timer}
import com.petclinic.context.AppCtx
import com.petclinic.database.DatabaseModule
import com.petclinic.logging.Logger.LogCtx
import distage.{ModuleDef, TagK}
import tofu.{WithContext, WithLocal, WithProvide, WithRun}
import tofu.lift.{Lift, UnliftIO}

import scala.annotation.nowarn

@nowarn("cat=unused-params")
final class MainModule[
  I[_] : TagK,
  F[_] : Concurrent : ContextShift : Timer : UnliftIO : WithRun[*[_], I, AppCtx] : TagK,
] extends ModuleDef {

  addImplicit[UnliftIO[F]]
  addImplicit[Clock[F]]
  addImplicit[Timer[F]]

  addImplicit[Concurrent[F]]
    .aliased[Sync[F]]
    .aliased[Functor[F]]
    .aliased[Applicative[F]]
    .aliased[Monad[F]]
    .aliased[MonadThrow[F]]
    .aliased[ApplicativeThrow[F]]

  addImplicit[WithRun[F, I, AppCtx]]
    .aliased[WithProvide[F, I, AppCtx]]
    .aliased[Lift[I, F]]
    .aliased[WithLocal[F, AppCtx]]
    .aliased[WithContext[F, AppCtx]]

  make[WithContext[F, LogCtx]].fromEffect { (L: WithLocal[F, AppCtx]) =>
    new WithContext[F, LogCtx] {
      def functor: Functor[F] = L.functor
      def context: F[LogCtx] = L.functor.map(L.context)(_.asLogCtx)
    }
  }

  include(new DatabaseModule[I, F])

}