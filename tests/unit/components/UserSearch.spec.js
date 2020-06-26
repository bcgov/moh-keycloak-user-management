import { mount, shallowMount } from '@vue/test-utils';
import UserSearch from '@/components/UserSearch.vue';
import Vuetify from 'vuetify';

//Mock out Repository Factory
jest.mock('@/api/RepositoryFactory', () => ({
    RepositoryFactory: { get: jest.fn() }
}));

//Mock out Search User method
const mockMethods = {
    searchUser: jest.fn(),
    goToCreateUser: jest.fn()
}

//Factory to to create wrapper
const factory = (values = {}) => {
    return shallowMount(UserSearch, {
        data() {
            return {
                ...values
            }
        },
        methods: mockMethods
    })
}

describe('UserSearch.vue', () => {

    it('renders the Search Button', () => {
        const wrapper = factory();
        expect(wrapper.find('#search-button').text()).toMatch('Search Users');
    });
    it('calls the searchUser method when Search button is clicked', () => {
        const wrapper = factory();
        wrapper.find('#search-button').trigger('click');
        expect(mockMethods.searchUser).toHaveBeenCalled();
    });
    it('calls the goToCreateUser method when Create New User button is clicked', () => {
        const wrapper = factory();
        wrapper.find('#create-user-button').trigger('click');
        expect(mockMethods.goToCreateUser).toHaveBeenCalled();
    });
    it('populates the table when there is data', () => {

        let fullWrapper = mount(UserSearch, {
            data() {
                return {
                    searchResults: [{ username: 'uName', firstName: 'fName', lastName: 'lName', email: 'some_email' }]
                }
            },
            methods: mockMethods,
            vuetify: new Vuetify() //needed mounting veutify components
        })
        const tableBody = fullWrapper.find('tbody')
        expect(tableBody.findAll('td').at(0).text()).toMatch('uName');
        expect(tableBody.findAll('td').at(1).text()).toMatch('fName');
        expect(tableBody.findAll('td').at(2).text()).toMatch('lName');
        expect(tableBody.findAll('td').at(3).text()).toMatch('some_email');
    });
});