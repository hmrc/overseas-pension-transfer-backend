/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.overseaspensiontransferbackend.transformers

import play.api.libs.json._

class NestedTransformer(path: JsPath, steps: Seq[TransformerStep]) extends Transformer {

  private def applyPipeline(json: JsObject, f: TransformerStep => JsObject => Either[JsError, JsObject]): Either[JsError, JsObject] =
    path.asSingleJson(json).asOpt[JsObject] match {
      case Some(nestedJson: JsObject) =>
        steps.foldLeft[Either[JsError, JsObject]](Right(nestedJson)) {
          case (Right(acc), step) => f(step)(acc)
          case (err @ Left(_), _) => err
        }.map(transformed => deepMergeAt(path, json, transformed))
      case _                          => Right(json)

    }

  private def deepMergeAt(path: JsPath, base: JsObject, update: JsObject): JsObject = {
    path.path match {
      case Seq(KeyPathNode(key))                        =>
        base + (key -> update)
      case Seq(KeyPathNode(parent), KeyPathNode(child)) =>
        val nested  = (base \ parent).asOpt[JsObject].getOrElse(Json.obj())
        val updated = nested + (child -> update)
        base + (parent -> updated)
      case _                                            =>
        throw new NotImplementedError("Only supports shallow or 2-level JsPaths for now.")
    }
  }

  override def applyCleanseTransforms(json: JsObject): Either[JsError, JsObject] =
    applyPipeline(json, _.cleanse)

  override def applyEnrichTransforms(json: JsObject): Either[JsError, JsObject] =
    applyPipeline(json, _.enrich)
}
