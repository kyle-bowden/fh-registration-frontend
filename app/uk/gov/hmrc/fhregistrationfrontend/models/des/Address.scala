/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.fhregistrationfrontend.models.des

import play.api.data.Forms._
import play.api.data.Mapping
import play.api.libs.json.{Json, OFormat}

case class Address(line1: String,
                   line2: Option[String],
                   line3: Option[String],
                   line4: Option[String],
                   postalCode: Option[String],
                   countryCode: String)

object Address {
  implicit val addressFormat: OFormat[Address] = Json.format[Address]

  def addressMapping: Mapping[Address] =
    mapping(
      "line1" -> nonEmptyText,
      "line2" -> optional(nonEmptyText),
      "line3" -> optional(nonEmptyText),
      "line4" -> optional(nonEmptyText),
      "postalCode" -> optional(nonEmptyText),
      "countryCode" -> nonEmptyText
    )(Address.apply)(Address.unapply)

}