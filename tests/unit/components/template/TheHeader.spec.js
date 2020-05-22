import { shallowMount } from '@vue/test-utils';
import TheHeader from '@/components/template/TheHeader.vue';

const mockMethods = {
    logout: jest.fn()
}

const factory = () => {
    return shallowMount(TheHeader, {
        methods: mockMethods
    })
}


describe('TheHeader.vue', () => {

    it('renders the logo', () => {
        const wrapper = factory();
        const img = wrapper.findAll('.logo');
        expect(img.length).toBe(1);
    });
    it('renders Sign Out', () => {
        const wrapper = factory();
        expect(wrapper.text()).toMatch('Sign Out');
    });
    it('calls the logout method when the Sign Out link is clicked', () => {
        const wrapper = factory();
        wrapper.find('a.sign-out').trigger('click');
        expect(mockMethods.logout).toHaveBeenCalled();
    });
});
