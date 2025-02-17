/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.fhregistrationfrontend.teststubs

import uk.gov.hmrc.auth.core.{AffinityGroup, Assistant, User}

trait UserTestData {
  val testUserId = "Int-uid"
  val ggEmail = "gg@test.com"
  val registrationNumber = "XZFH00000123456"
  val adminRole = User
  val assistantRole = Assistant
  val userAffinityGroup = AffinityGroup.Individual

}
