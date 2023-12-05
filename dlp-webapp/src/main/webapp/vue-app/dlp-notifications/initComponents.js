import dlpItemNotificationPlugin from './components/DlpItemNotificationPlugin.vue';

const components = {
  'dlp-item-notification': dlpItemNotificationPlugin,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
