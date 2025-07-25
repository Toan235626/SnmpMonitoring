import axios from 'axios';

const baseURL = import.meta.env.VITE_API_BASE_URL || '/api';

export default axios.create({
  baseURL,
});