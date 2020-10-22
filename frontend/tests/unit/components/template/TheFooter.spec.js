import { shallowMount } from '@vue/test-utils';
import TheFooter from '@/components/template/TheFooter.vue';

const factory = () => {
  return shallowMount(TheFooter, {
  })
}

describe('TheFooter.vue', () => {
  it('renders Contact Us', () => {
    const wrapper = factory();
    expect(wrapper.text()).toMatch('Contact Us');
  });
  it('renders Disclaimer', () => {
    const wrapper = factory();
    expect(wrapper.text()).toMatch('Disclaimer');
  });
  it('renders Accessibility', () => {
    const wrapper = factory();
    expect(wrapper.text()).toMatch('Accessibility');
  });
  it('renders Privacy', () => {
    const wrapper = factory();
    expect(wrapper.text()).toMatch('Privacy');
  });
  it('renders Copyright', () => {
    const wrapper = factory();
    expect(wrapper.text()).toMatch('Copyright');
  });
});