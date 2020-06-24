import ClientsRepository from './ClientsRepository';
import UsersRepository from './UsersRepository';
import EventsRepository from "./EventsRepository";
import AdminEventsRepository from "./AdminEventsRepository";

const repositories = {
    clients: ClientsRepository,
    users: UsersRepository,
    events: EventsRepository,
    adminEvents: AdminEventsRepository
};

export const RepositoryFactory = {
  get: name => repositories[name]  
};