import { useState, useEffect } from 'react';
import { getAddressesByUser, createAddress, updateAddress, deleteAddress, setDefaultAddress } from '../../services/addressService';
import { useAuth } from '../../context/AuthContext';
import { MapPin, Plus, Trash2, Home, Briefcase, Star, CheckCircle } from 'lucide-react';
import { showErrorAlert, showSuccessToast, showWarningToast } from '../../utils/errorHandler';

const AddressManagement = () => {
    const { user } = useAuth();
    const [addresses, setAddresses] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showForm, setShowForm] = useState(false);
    const [editingAddress, setEditingAddress] = useState(null);
    const [formData, setFormData] = useState({
        addressType: 'HOME',
        streetAddress: '',
        city: '',
        state: '',
        postalCode: '',
        country: '',
        isDefault: false
    });

    useEffect(() => {
        if (user) loadAddresses();
    }, [user]);

    const loadAddresses = async () => {
        try {
            setLoading(true);
            const response = await getAddressesByUser(user.userId);
            setAddresses(response.data || []);
        } catch (error) {
            console.error('Error loading addresses:', error);
            showErrorAlert(error, 'Failed to load addresses');
        } finally {
            setLoading(false);
        }
    };

    const handleInputChange = (e) => {
        const { name, value, type, checked } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: type === 'checkbox' ? checked : value
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            if (editingAddress) {
                await updateAddress(editingAddress.addressId, formData);
                showSuccessToast('Address updated successfully');
            } else {
                await createAddress({ ...formData, userId: user.userId });
                showSuccessToast('Address added successfully');
            }
            setShowForm(false);
            setEditingAddress(null);
            resetForm();
            loadAddresses();
        } catch (error) {
            showErrorAlert(error, editingAddress ? 'Failed to update address' : 'Failed to add address');
        }
    };

    const handleEdit = (address) => {
        setEditingAddress(address);
        setFormData({
            addressType: address.addressType,
            streetAddress: address.streetAddress,
            city: address.city,
            state: address.state,
            postalCode: address.postalCode,
            country: address.country,
            isDefault: address.isDefault
        });
        setShowForm(true);
    };

    const handleDelete = async (id) => {
        if (window.confirm('Delete this address?')) {
            try {
                await deleteAddress(id);
                showSuccessToast('Address deleted successfully');
                loadAddresses();
            } catch (error) {
                showErrorAlert(error, 'Failed to delete address');
            }
        }
    };

    const handleSetDefault = async (id) => {
        try {
            await setDefaultAddress(id);
            showSuccessToast('Default address updated');
            loadAddresses();
        } catch (error) {
            showErrorAlert(error, 'Failed to set default address');
        }
    };

    const resetForm = () => {
        setFormData({
            addressType: 'HOME',
            streetAddress: '',
            city: '',
            state: '',
            postalCode: '',
            country: '',
            isDefault: false
        });
    };

    const getTypeIcon = (type) => {
        switch (type) {
            case 'HOME': return <Home className="w-4 h-4" />;
            case 'WORK': return <Briefcase className="w-4 h-4" />;
            default: return <MapPin className="w-4 h-4" />;
        }
    };

    return (
        <div className="space-y-8 animate-in fade-in duration-500">
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-3xl font-black text-gray-900 tracking-tight">Saved Addresses</h1>
                    <p className="text-gray-500 mt-1 font-medium">Manage your delivery locations for faster checkout.</p>
                </div>
                {!showForm && (
                    <button
                        onClick={() => setShowForm(true)}
                        className="btn-primary flex items-center gap-2 px-6 py-3"
                    >
                        <Plus className="w-5 h-5" />
                        Add New Address
                    </button>
                )}
            </div>

            {showForm && (
                <div className="bg-white p-8 rounded-3xl shadow-xl border border-gray-100 animate-in slide-in-from-top duration-300">
                    <h3 className="text-xl font-bold text-gray-900 mb-6">{editingAddress ? 'Update Address' : 'New Address'}</h3>
                    <form onSubmit={handleSubmit} className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div className="md:col-span-2">
                            <label className="text-xs font-black text-gray-400 uppercase tracking-widest block mb-2">Address Type</label>
                            <div className="flex gap-4">
                                {['HOME', 'WORK', 'OTHER'].map(type => (
                                    <button
                                        key={type}
                                        type="button"
                                        onClick={() => setFormData(prev => ({ ...prev, addressType: type }))}
                                        className={`flex items-center gap-2 px-6 py-3 rounded-2xl border transition-all font-bold text-sm ${formData.addressType === type
                                                ? 'bg-primary-50 border-primary-500 text-primary-700'
                                                : 'bg-white border-gray-100 text-gray-500 hover:border-gray-300'
                                            }`}
                                    >
                                        {getTypeIcon(type)}
                                        {type}
                                    </button>
                                ))}
                            </div>
                        </div>

                        <div className="md:col-span-2">
                            <label className="text-xs font-black text-gray-400 uppercase tracking-widest block mb-2">Street Address</label>
                            <input
                                required
                                name="streetAddress"
                                value={formData.streetAddress}
                                onChange={handleInputChange}
                                className="w-full bg-gray-50 border-none rounded-2xl py-4 px-6 focus:ring-2 focus:ring-primary-500 transition-all font-medium"
                                placeholder="House no, Street name, Area"
                            />
                        </div>

                        <div>
                            <label className="text-xs font-black text-gray-400 uppercase tracking-widest block mb-2">City</label>
                            <input
                                required
                                name="city"
                                value={formData.city}
                                onChange={handleInputChange}
                                className="w-full bg-gray-50 border-none rounded-2xl py-4 px-6 focus:ring-2 focus:ring-primary-500 transition-all font-medium"
                            />
                        </div>

                        <div>
                            <label className="text-xs font-black text-gray-400 uppercase tracking-widest block mb-2">State / Province</label>
                            <input
                                required
                                name="state"
                                value={formData.state}
                                onChange={handleInputChange}
                                className="w-full bg-gray-50 border-none rounded-2xl py-4 px-6 focus:ring-2 focus:ring-primary-500 transition-all font-medium"
                            />
                        </div>

                        <div>
                            <label className="text-xs font-black text-gray-400 uppercase tracking-widest block mb-2">Postal Code</label>
                            <input
                                required
                                name="postalCode"
                                value={formData.postalCode}
                                onChange={handleInputChange}
                                className="w-full bg-gray-50 border-none rounded-2xl py-4 px-6 focus:ring-2 focus:ring-primary-500 transition-all font-medium"
                            />
                        </div>

                        <div>
                            <label className="text-xs font-black text-gray-400 uppercase tracking-widest block mb-2">Country</label>
                            <input
                                required
                                name="country"
                                value={formData.country}
                                onChange={handleInputChange}
                                className="w-full bg-gray-50 border-none rounded-2xl py-4 px-6 focus:ring-2 focus:ring-primary-500 transition-all font-medium"
                            />
                        </div>

                        <div className="md:col-span-2 flex items-center gap-2">
                            <input
                                type="checkbox"
                                id="isDefault"
                                name="isDefault"
                                checked={formData.isDefault}
                                onChange={handleInputChange}
                                className="w-5 h-5 rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                            />
                            <label htmlFor="isDefault" className="text-sm font-bold text-gray-700">Set as default address</label>
                        </div>

                        <div className="md:col-span-2 flex gap-4 mt-4">
                            <button type="submit" className="flex-1 btn-primary py-4">
                                {editingAddress ? 'Update Address' : 'Save Address'}
                            </button>
                            <button
                                type="button"
                                onClick={() => { setShowForm(false); setEditingAddress(null); resetForm(); }}
                                className="flex-1 px-6 py-4 bg-gray-100 text-gray-700 font-black rounded-2xl hover:bg-gray-200 transition-all"
                            >
                                Cancel
                            </button>
                        </div>
                    </form>
                </div>
            )}

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {loading ? (
                    [1, 2, 3].map(i => (
                        <div key={i} className="h-48 bg-gray-100 rounded-3xl animate-pulse"></div>
                    ))
                ) : addresses.length === 0 ? (
                    <div className="md:col-span-3 py-24 text-center">
                        <div className="w-20 h-20 bg-gray-50 text-gray-200 rounded-full flex items-center justify-center mx-auto mb-6">
                            <MapPin className="w-10 h-10" />
                        </div>
                        <h4 className="text-xl font-bold text-gray-400">No addresses saved yet</h4>
                        <p className="text-gray-400 mt-2">Add your first address to get started!</p>
                    </div>
                ) : (
                    addresses.map((address) => (
                        <div
                            key={address.addressId}
                            className={`group relative bg-white p-6 rounded-3xl border transition-all duration-300 hover:shadow-xl ${address.isDefault ? 'border-primary-200 shadow-lg shadow-primary-50/50' : 'border-gray-100 hover:border-gray-300'
                                }`}
                        >
                            <div className="flex items-center justify-between mb-4">
                                <div className={`p-2 rounded-xl scale-90 ${address.isDefault ? 'bg-primary-50 text-primary-600' : 'bg-gray-50 text-gray-400'
                                    }`}>
                                    {getTypeIcon(address.addressType)}
                                </div>
                                <div className="flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                                    <button onClick={() => handleEdit(address)} className="p-2 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-xl transition-all">
                                        <Star className="w-4 h-4" />
                                    </button>
                                    <button onClick={() => handleDelete(address.addressId)} className="p-2 text-gray-400 hover:text-rose-600 hover:bg-rose-50 rounded-xl transition-all">
                                        <Trash2 className="w-4 h-4" />
                                    </button>
                                </div>
                            </div>

                            <div className="space-y-1">
                                <div className="flex items-center gap-2">
                                    <span className="text-xs font-black text-gray-400 uppercase tracking-widest">{address.addressType}</span>
                                    {address.isDefault && (
                                        <span className="flex items-center gap-1 text-[10px] bg-emerald-50 text-emerald-600 px-2 py-0.5 rounded-full font-black uppercase">
                                            <CheckCircle className="w-2 h-2" /> Default
                                        </span>
                                    )}
                                </div>
                                <p className="text-base font-bold text-gray-900 leading-tight mt-2">{address.streetAddress}</p>
                                <p className="text-sm text-gray-500 font-medium">{address.city}, {address.state} {address.postalCode}</p>
                                <p className="text-sm text-gray-500 font-medium">{address.country}</p>
                            </div>

                            {!address.isDefault && (
                                <button
                                    onClick={() => handleSetDefault(address.addressId)}
                                    className="mt-6 w-full py-3 text-[10px] font-black uppercase tracking-widest text-primary-600 bg-primary-50/50 rounded-2xl opacity-0 group-hover:opacity-100 transition-all hover:bg-primary-50"
                                >
                                    Set as Default
                                </button>
                            )}
                        </div>
                    ))
                )}
            </div>
        </div>
    );
};

export default AddressManagement;
