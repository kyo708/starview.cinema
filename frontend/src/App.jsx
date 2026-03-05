import Catalog from './components/Catalog/Catalog.jsx';
import Navbar from './components/Navbar/Navbar.jsx';
import './App.css';

function App() {
  return (
    <div>
      {/* Đây là cách chúng ta gọi linh kiện Catalog ra để hiển thị */}
      <Navbar />
      <Catalog /> 
    </div>
  )
}

export default App;