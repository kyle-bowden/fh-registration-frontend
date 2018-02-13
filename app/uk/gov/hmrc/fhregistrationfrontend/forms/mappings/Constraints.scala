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

package uk.gov.hmrc.fhregistrationfrontend.forms.mappings

import play.api.data.validation.{Constraint, Invalid, Valid}

object Constraints extends play.api.data.validation.Constraints {

  def oneOfConstraint[T](options: Seq[T]): Constraint[T] = Constraint { v ⇒
    if (options contains v)
      Valid
    else
      Invalid("todo.choose.one.of")
  }

}
