/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright © 2023-2024 Carl Dea.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Test Module Axonic a library for finite state machines in Java.
 */
module org.carlfx.axonic.test {

    requires org.slf4j;
    requires org.carlfx.axonic;
    requires org.junit.jupiter.engine;
    requires org.junit.jupiter.params;
    requires org.junit.jupiter.api;
    opens org.carlfx.axonic.test to org.junit.platform.commons;

    exports org.carlfx.axonic.test;
}
