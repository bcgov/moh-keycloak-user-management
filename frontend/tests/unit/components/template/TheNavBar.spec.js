import { shallowMount } from '@vue/test-utils';
import TheNavBar from '@/components/template/TheNavBar.vue';

const factory = ($route = {}) => {
    return shallowMount(TheNavBar, {
        mocks: {
            $route
        },
        stubs: ['router-link', 'router-view']
    })
}

describe('TheNavBar.vue', () => {

    it('shows Users link as active when a Users child route is the current route', () => {
        const wrapper = factory({ name: 'UserSearch' });
        const usersLink = wrapper.find('#users-link');
        expect(usersLink.classes()).toContain('active');
    });
    it('shows Users link as active when a Users child route is the current route', () => {
        const wrapper = factory({ name: 'UserUpdate' });
        const usersLink = wrapper.find('#users-link');
        expect(usersLink.classes()).toContain('active');
    });
    it('shows Users link as active when a Users child route is the current route', () => {
        const wrapper = factory({ name: 'UserCreate' });
        const usersLink = wrapper.find('#users-link');
        expect(usersLink.classes()).toContain('active');
    });
    it('shows Event Log link as active when EventLog is the current route', () => {
        const wrapper = factory({ name: 'EventLog' });
        const usersLink = wrapper.find('#event-log-link');
        expect(usersLink.classes()).toContain('active');
    });
    it('shows Admin Event Log link as active when AdminEventLog is the current route', () => {
        const wrapper = factory({ name: 'AdminEventLog' });
        const usersLink = wrapper.find('#admin-event-log-link');
        expect(usersLink.classes()).toContain('active');
    });

});