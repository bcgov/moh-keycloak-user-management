import { shallowMount } from '@vue/test-utils';
import UserSearch from '@/components/UserSearch.vue';

//Mock out Repository Factory
jest.mock('@/api/RepositoryFactory', () => ({
    RepositoryFactory: { get: jest.fn() }
}));

//Mock out Search User method
const mockMethods = {
    searchUser: jest.fn()
}

//Factory to to create wrapper
const factory = (values = {}) => {
    return shallowMount(UserSearch, {  
        data () {
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
});