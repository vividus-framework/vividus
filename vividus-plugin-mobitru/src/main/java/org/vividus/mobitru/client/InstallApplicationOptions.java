/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.mobitru.client;

/**
 * Application installation options as described in
 * <a href="https://app.mobitru.com/wiki/apidoc/#api-InstallApp-InstallApp">API Docs</a>
 *
 * @param resignIosApp     (iOS only) Resign the application (*.ipa) with Mobitru profile or not.
 * @param injectionEnabled Inject special code into application to allow emulation of "touch id" action and QR code
 *                         scan.
 */
public record InstallApplicationOptions(boolean resignIosApp, boolean injectionEnabled)
{
}
