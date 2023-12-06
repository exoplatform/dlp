<!--
 * Copyright (C) 2023 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
-->
<template>
  <user-notification-template
      :notification="notification"
      :message="message">
    <template #avatar>
      <div>
        <v-icon size="34">fas fa-shield-alt</v-icon>
      </div>
    </template>
    <template #actions>
      <v-icon size="16">{{ icon }}</v-icon>
      <span>{{ documentName }}</span>
    </template>
  </user-notification-template>
</template>

<script>
export default {
  props: {
    notification: {
      type: Object,
      default: null,
    },
  },
  computed: {
    message() {
      if (this.notification?.plugin === 'DlpAdminDetectedItemPlugin') {
        return this.$t('Notification.intranet.message.DlpAdminDetectedItemPlugin');
      }
      if (this.notification?.plugin === 'DlpUserDetectedItemPlugin') {
        return this.$t('Notification.intranet.message.DlpUserDetectedItemPlugin');
      }
      if (this.notification?.plugin === 'DlpUserRestoredItemPlugin') {
        return this.$t('Notification.intranet.message.DlpUserRestoredItemPlugin');
      }
      return '';
    },
    documentName() {
      return this.notification?.parameters?.itemTitle;
    },
    icon() {
      return this.notification?.plugin === 'DlpUserRestoredItemPlugin' ? 'fa-folder' : 'fa-folder-open';
    }
  }
};
</script>