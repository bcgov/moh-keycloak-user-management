import ClientsRepository from "./ClientsRepository";
import UsersRepository from "./UsersRepository";

const repositories = {
  clients: ClientsRepository,
  users: UsersRepository,
};

export const RepositoryFactory = {
  get: (name) => repositories[name],
};
