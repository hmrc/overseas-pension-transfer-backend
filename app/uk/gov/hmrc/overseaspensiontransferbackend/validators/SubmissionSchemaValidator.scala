/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.overseaspensiontransferbackend.validators

import com.networknt.schema.{Error, InputFormat, SchemaLocation, SchemaRegistry}
import com.networknt.schema.dialect.Dialects.getOpenApi30
import play.api.libs.json.JsValue

import javax.inject.{Inject, Singleton}
import scala.jdk.CollectionConverters.*

@Singleton
class SubmissionSchemaValidator @Inject() () {

  def validate(payload: JsValue): Set[Error] = {
    val schemaLocation =
      "classpath:/resources/submission-schema.yaml#/paths/~1RESTAdapter~1pods~1reports~1qrops-transfer/post/requestBody/content/application~1json/schema"

    val schema =
      SchemaRegistry.withDialect(getOpenApi30).getSchema(SchemaLocation.of(schemaLocation))

    schema.validate(payload.toString, InputFormat.JSON).asScala.toSet
  }
}
